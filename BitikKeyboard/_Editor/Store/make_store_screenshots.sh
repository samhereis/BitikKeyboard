#!/bin/bash
#
# Batch export of the Play Store banners, all languages, in ONE launch (the Android counterpart of the iOS "Export all"):
#
#   ~/Desktop/ShoktukKeyboardScreenshots/Android/Android_<lang>/Android_<lang>_<Banner>.png      (<lang> is en, ky or tr)
#
# The debug-only ScreenshotExporterActivity (app/src/debug/.../store/ScreenshotExporter.kt) draws every banner with the real
# keyboard, saves them on the device, and this script pulls them to the Desktop folder above. To browse the banners and export from the
# screen instead, open that activity (or its preview in Android Studio).
#
# Usage:
#   _Editor/Store/make_store_screenshots.sh                       # every banner, every language
#   _Editor/Store/make_store_screenshots.sh -l "en tr"            # only these languages
#   _Editor/Store/make_store_screenshots.sh -e <serial>           # which device (a real phone is much faster than an emulator)
#   _Editor/Store/make_store_screenshots.sh --skip-build          # reuse the installed app

set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$HERE/../.." && pwd)"
PACKAGE="com.shoktuk.shoktukkeyboard"
DEVICE_DIR="/sdcard/Android/data/$PACKAGE/files/store"

LANGUAGES="en ky_kiril tr"
DEVICE=""; BUILD=1
while [[ $# -gt 0 ]]; do
  case "$1" in
    -l) LANGUAGES="$2"; shift 2 ;;
    -e) DEVICE="$2"; shift 2 ;;
    --skip-build) BUILD=0; shift ;;
    *) echo "Unknown option: $1"; exit 1 ;;
  esac
done

command -v adb >/dev/null || { echo "adb not found (Android SDK platform-tools)."; exit 1; }
if [[ -z "$DEVICE" ]]; then
  DEVICE="$(adb devices | awk 'NR>1 && $2=="device" {print $1}' | head -1)"
  [[ -n "$DEVICE" ]] || { echo "No device. Start an emulator or connect a phone."; exit 1; }
fi
ADB=(adb -s "$DEVICE")

if [[ "$BUILD" == "1" ]]; then
  echo "Building…"
  (cd "$PROJECT_DIR" && gradle -q :app:installDebug -Pandroid.injected.device.serial="$DEVICE" >/dev/null)
fi
"${ADB[@]}" shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1 || true
# No "Viewing full screen" pop-up over the banners.
"${ADB[@]}" shell settings put secure immersive_mode_confirmations confirmed >/dev/null 2>&1 || true

"${ADB[@]}" shell rm -rf "$DEVICE_DIR" >/dev/null 2>&1 || true
"${ADB[@]}" shell am force-stop "$PACKAGE"
echo "Exporting ($LANGUAGES)…"
"${ADB[@]}" shell am start -n "$PACKAGE/.store.ScreenshotExporterActivity" --es autoexport "'$LANGUAGES'" >/dev/null

# The exporter writes a "done" file when the last banner is saved.
for _ in $(seq 1 900); do
  "${ADB[@]}" shell "test -f $DEVICE_DIR/done" 2>/dev/null && break
  sleep 2
done
"${ADB[@]}" shell "test -f $DEVICE_DIR/done" 2>/dev/null || { echo "Timed out waiting for the export."; exit 1; }

"$HERE/pull_store_screenshots.sh" -e "$DEVICE"
"${ADB[@]}" shell am force-stop "$PACKAGE"
