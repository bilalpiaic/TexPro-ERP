#!/usr/bin/env bash
# Build an installable standalone APK at dist/TexPro-ERP.apk
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -x "./gradlew" ]]; then
  ./gradlew --no-daemon :app:copySideloadApk
else
  gradle --no-daemon :app:copySideloadApk
fi

echo "APK: ${ROOT}/dist/TexPro-ERP.apk"
ls -lh "${ROOT}/dist/TexPro-ERP.apk"
