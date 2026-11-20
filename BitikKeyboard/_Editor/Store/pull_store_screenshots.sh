#!/bin/bash
#
# Brings the banners the debug ScreenshotExporterActivity saved on the device to the Desktop:
#   ~/Desktop/ShoktukKeyboardScreenshots/Android/Android_<lang>/Android_<lang>_<Banner>.png
# (the iPhone / iPad exporter fills ~/Desktop/ShoktukKeyboardScreenshots/IPhone and /IPad the same way).
# Start the exporter on the device first:  adb shell am start -n com.shoktuk.shoktukkeyboard/.store.ScreenshotExporterActivity
#
#   _Editor/Store/pull_store_screenshots.sh [-e <device serial>]

set -euo pipefail
PACKAGE="com.shoktuk.shoktukkeyboard"
OUT="$HOME/Desktop/ShoktukKeyboardScreenshots/Android"
DEVICE_ARGS=()
[[ "${1:-}" == "-e" ]] && DEVICE_ARGS=(-s "$2")

TMP="$(mktemp -d)"
adb "${DEVICE_ARGS[@]}" pull "/sdcard/Android/data/$PACKAGE/files/store/." "$TMP" >/dev/null
for folder in "$TMP"/Android_*; do
  [[ -d "$folder" ]] || continue
  mkdir -p "$OUT/$(basename "$folder")"
  cp "$folder"/*.png "$OUT/$(basename "$folder")/"
done
rm -rf "$TMP"
echo "Done: $OUT"
