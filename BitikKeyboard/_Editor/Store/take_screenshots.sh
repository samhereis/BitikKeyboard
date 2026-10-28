#!/bin/bash
#
# Takes the Play Store screenshots of the app in every language with no manual steps (the Android counterpart of the
# iOS script). It installs the debug build, then for each language and screen starts the app on that screen
# (MainActivity reads `destination` and, in debug builds, `screenshot_language`) and saves the picture to
#
#   _Editor/Store/Screenshots/<language>/<NN>_<screen>.png
#
# Usage:
#   _Editor/Store/take_screenshots.sh                          # all languages, all screens
#   _Editor/Store/take_screenshots.sh -l "en tr"               # only these languages
#   _Editor/Store/take_screenshots.sh -s "settings savables"   # only these screens
#   _Editor/Store/take_screenshots.sh -e emulator-5554         # which device (default: the only one running)
#   _Editor/Store/take_screenshots.sh --skip-build             # reuse the installed app
#
# Needs a running emulator or phone (adb devices) and JAVA_HOME / gradle as for a normal build.

set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$HERE/../.." && pwd)"
OUT="$HERE/Screenshots"
PACKAGE="com.shoktuk.shoktukkeyboard"
IME="$PACKAGE/.keyboard.MyKeyboardService"

LANGUAGES=(ky_latin ky_kiril en tr kz az ru)
# <name>:<destination> pairs; the order is the file numbering. `keyboard` opens the test screen with the keyboard up.
ALL_SCREENS=(keyboard:test_keyboard basicInfo:basic_info settings:settings mainSettings:mainSettings appearance:appearance savables:savables howToEnable:how_to_enable)
SCREENS=("${ALL_SCREENS[@]}")
WAIT_SECONDS="${WAIT_SECONDS:-3}"

DEVICE=""
BUILD=1
while [[ $# -gt 0 ]]; do
  case "$1" in
    -l) read -r -a LANGUAGES <<< "$2"; shift 2 ;;
    -s)
      SCREENS=()
      for wanted in $2; do
        for entry in "${ALL_SCREENS[@]}"; do [[ "${entry%%:*}" == "$wanted" ]] && SCREENS+=("$entry"); done
      done
      shift 2 ;;
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

# Start from the default settings (debug builds only), so earlier experiments don't show up in the pictures.
"${ADB[@]}" shell am force-stop "$PACKAGE"
"${ADB[@]}" shell run-as "$PACKAGE" rm -f shared_prefs/settings_prefs.xml >/dev/null 2>&1 || true

# The handwriting intro dialog would cover the keyboard.
"${ADB[@]}" shell settings put secure stylus_handwriting_enabled 0 >/dev/null 2>&1 || true
select_keyboard() { # stopping the app makes Android fall back to its default keyboard, so select ours again
  "${ADB[@]}" shell ime enable "$IME" >/dev/null 2>&1 || true
  "${ADB[@]}" shell ime set "$IME" >/dev/null 2>&1 || true
}

# Same status bar every time (demo mode).
"${ADB[@]}" shell settings put global sysui_demo_allowed 1 >/dev/null 2>&1 || true
"${ADB[@]}" shell am broadcast -a com.android.systemui.demo -e command enter >/dev/null 2>&1 || true
"${ADB[@]}" shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941 >/dev/null 2>&1 || true
"${ADB[@]}" shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false >/dev/null 2>&1 || true
"${ADB[@]}" shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 >/dev/null 2>&1 || true
trap '"${ADB[@]}" shell am broadcast -a com.android.systemui.demo -e command exit >/dev/null 2>&1 || true' EXIT

size="$("${ADB[@]}" shell wm size | awk -F'[ x]+' '/Physical/ {print $3, $4}')"
read -r width height <<< "$size"

for lang in "${LANGUAGES[@]}"; do
  mkdir -p "$OUT/$lang"
  for entry in "${SCREENS[@]}"; do
    name="${entry%%:*}"; destination="${entry##*:}"
    index=0
    for i in "${!ALL_SCREENS[@]}"; do [[ "${ALL_SCREENS[$i]}" == "$entry" ]] && index=$((i + 1)); done
    file="$OUT/$lang/$(printf '%02d' "$index")_${name}.png"
    echo "[$lang] $name"
    "${ADB[@]}" shell am force-stop "$PACKAGE"
    "${ADB[@]}" shell am start -n "$PACKAGE/.MainActivity" --es screenshot_language "$lang" --es destination "$destination" >/dev/null
    sleep "$WAIT_SECONDS"
    if [[ "$name" == "keyboard" ]]; then
      select_keyboard
      # Tap the input field (lower middle of the screen) to bring the keyboard up.
      "${ADB[@]}" shell input tap "$((width / 2))" "$((height * 55 / 100))"
      sleep 2
    fi
    "${ADB[@]}" exec-out screencap -p > "$file"
  done
done

echo "Done: $OUT"
