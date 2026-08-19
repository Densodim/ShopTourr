#!/usr/bin/env bash
# Fail if any measured IPA exceeds the install budget (default 40 MiB).
# Skip (exit 0) when no IPA has been archived yet — CI does not produce one on every PR.
set -euo pipefail

BUDGET_BYTES="${APP_SIZE_BUDGET_BYTES:-$((40 * 1024 * 1024))}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IPA_DIR="${1:-$ROOT/iosApp/build}"

if [[ ! -d "$IPA_DIR" ]]; then
  echo "IPA directory not found: $IPA_DIR (skip)"
  exit 0
fi

ipas=()
while IFS= read -r line; do
  ipas+=("$line")
done < <(find "$IPA_DIR" -type f -name '*.ipa' | sort)

if [[ ${#ipas[@]} -eq 0 ]]; then
  echo "No IPAs under $IPA_DIR (skip)"
  exit 0
fi

failed=0
echo "iOS size budget: ${BUDGET_BYTES} bytes ($((BUDGET_BYTES / 1024 / 1024)) MiB)"
for ipa in "${ipas[@]}"; do
  size="$(wc -c <"$ipa" | tr -d ' ')"
  rel="${ipa#"$ROOT/"}"
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
echo "All IPAs within budget."
