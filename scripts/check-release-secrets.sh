#!/usr/bin/env bash
# Exit 1 if required store-upload environment variables are missing.
set -euo pipefail

usage() {
  echo "Usage: $0 android|ios" >&2
  exit 2
}

[[ $# -eq 1 ]] || usage

android_keys=(
  ANDROID_KEYSTORE_FILE
  ANDROID_KEYSTORE_PASSWORD
  ANDROID_KEY_ALIAS
  ANDROID_KEY_PASSWORD
  PLAY_JSON_KEY_FILE
)

ios_keys=(
  APP_STORE_CONNECT_KEY_ID
  APP_STORE_CONNECT_ISSUER_ID
  APP_STORE_CONNECT_KEY_P8
)

case "$1" in
  android) keys=("${android_keys[@]}") ;;
  ios) keys=("${ios_keys[@]}") ;;
  *) usage ;;
esac

missing=()
for key in "${keys[@]}"; do
  if [[ -z "${!key:-}" ]]; then
    missing+=("$key")
  fi
done

if (( ${#missing[@]} > 0 )); then
  echo "Missing release secrets for $1:" >&2
  printf '  %s\n' "${missing[@]}" >&2
  exit 1
fi

if [[ "$1" == "android" ]]; then
  if [[ ! -f "$ANDROID_KEYSTORE_FILE" ]]; then
    echo "ANDROID_KEYSTORE_FILE is not a file: $ANDROID_KEYSTORE_FILE" >&2
    exit 1
  fi
  if [[ ! -f "$PLAY_JSON_KEY_FILE" ]]; then
    echo "PLAY_JSON_KEY_FILE is not a file: $PLAY_JSON_KEY_FILE" >&2
    exit 1
  fi
fi

echo "Release secrets for $1 are present."
