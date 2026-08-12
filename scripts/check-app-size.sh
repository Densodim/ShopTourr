#!/usr/bin/env bash
# Fail if any measured APK exceeds the install budget (default 40 MiB).
set -euo pipefail

BUDGET_BYTES="${APP_SIZE_BUDGET_BYTES:-$((40 * 1024 * 1024))}"
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

failed=0
echo "App size budget: ${BUDGET_BYTES} bytes ($((BUDGET_BYTES / 1024 / 1024)) MiB)"
for apk in "${apks[@]}"; do
  size="$(wc -c <"$apk" | tr -d ' ')"
  rel="${apk#"$ROOT/"}"
  mib=$(( size / 1024 / 1024 ))
  rem=$(( (size % (1024 * 1024)) * 10 / (1024 * 1024) ))
  printf '  %s  %s bytes (%s.%s MiB)\n' "$rel" "$size" "$mib" "$rem"
  if (( size > BUDGET_BYTES )); then
    echo "OVER BUDGET: $rel" >&2
    failed=1
  fi
done

if (( failed )); then
  exit 1
fi
echo "All APKs within budget."
