#!/usr/bin/env bash
# Fail if arm64-v8a native libraries in an APK are not 16 KB page-aligned.
# Play requires this for Android 15+ 16 KB devices.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APK_DIR="${1:-$ROOT/androidApp/build/outputs/apk}"

if [[ ! -d "$APK_DIR" ]]; then
  echo "APK directory not found: $APK_DIR" >&2
  echo "Build first, e.g. ./gradlew :androidApp:assembleDebug" >&2
  exit 2
fi

apks=()
while IFS= read -r line; do
  apks+=("$line")
done < <(find "$APK_DIR" -type f -name '*.apk' | sort)

if [[ ${#apks[@]} -eq 0 ]]; then
  echo "No APKs under $APK_DIR" >&2
  exit 2
fi

python3 - "$ROOT" "${apks[@]}" <<'PY'
import struct
import sys
import zipfile
from pathlib import Path

PT_LOAD = 1
MIN_ALIGN = 0x4000  # 16 KiB
checked = 0
failed = []

def load_alignments(so_bytes: bytes) -> list[int]:
    if so_bytes[:4] != b"\x7fELF":
        return []
    ei_class = so_bytes[4]
    ei_data = so_bytes[5]
    endian = "<" if ei_data == 1 else ">"
    if ei_class == 2:  # ELF64
        e_phoff = struct.unpack_from(endian + "Q", so_bytes, 32)[0]
        e_phentsize = struct.unpack_from(endian + "H", so_bytes, 54)[0]
        e_phnum = struct.unpack_from(endian + "H", so_bytes, 56)[0]
        p_type_off, p_align_off, p_align_fmt = 0, 48, "Q"
    elif ei_class == 1:  # ELF32 — 16 KB devices are 64-bit; skip
        return []
    else:
        return []
    aligns = []
    for i in range(e_phnum):
        off = e_phoff + i * e_phentsize
        p_type = struct.unpack_from(endian + "I", so_bytes, off + p_type_off)[0]
        if p_type != PT_LOAD:
            continue
        aligns.append(struct.unpack_from(endian + p_align_fmt, so_bytes, off + p_align_off)[0])
    return aligns

root = Path(sys.argv[1])
for apk in sys.argv[2:]:
    apk_path = Path(apk)
    try:
        rel = str(apk_path.relative_to(root))
    except ValueError:
        rel = apk
    with zipfile.ZipFile(apk) as zf:
        sos = [n for n in zf.namelist() if n.startswith("lib/arm64-v8a/") and n.endswith(".so")]
        if not sos:
            print(f"  {rel}: no arm64-v8a .so (skip)")
            continue
        for name in sos:
            aligns = load_alignments(zf.read(name))
            if not aligns:
                print(f"  {rel}: {name} has no PT_LOAD (skip)")
                continue
            checked += 1
            worst = min(aligns)
            ok = all(a >= MIN_ALIGN for a in aligns)
            status = "OK" if ok else "FAIL"
            print(f"  {rel}: {name} min p_align={worst} ({status})")
            if not ok:
                failed.append(f"{rel}:{name}")

print(f"Checked {checked} arm64-v8a libraries for 16 KB LOAD alignment.")
if failed:
    print("Not 16 KB aligned:", ", ".join(failed), file=sys.stderr)
    sys.exit(1)
if checked == 0:
    print("No arm64-v8a PT_LOAD libraries found — nothing to enforce.")
PY
