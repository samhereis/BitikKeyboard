#!/bin/bash
# Typing benchmark. Usage: ./bench_typing.sh [keys=800] [ms between keys=60] [corpus file=bench/corpus_ky_cyr.txt] [touch=1] [bitik=0] [tap ms=45] [drift px=0]
# bitik=1: the corpus is Cyrillic and is typed with the Bitik glyph keys that spell it (keyboard must be in Bitik mode)
# touch=1 (default) sends real touch events at each key's position; touch=0 calls the key handler directly.
# Types real text through the keyboard's own key path in the focused text field and prints every slow step and every
# main-thread freeze (STALL). ~60 ms between keys is fast typing (16 keys/s).
set -euo pipefail
cd "$(dirname "$0")"
COUNT="${1:-800}"; INTERVAL="${2:-60}"; CORPUS="${3:-bench/corpus_ky_cyr.txt}"; TOUCH="${4:-1}"; BITIK="${5:-0}"; TAP="${6:-45}"; DRIFT="${7:-0}"
B64="$(tr -d '\n' < /dev/null; python3 -c "import base64,sys;print(base64.b64encode(open(sys.argv[1],'rb').read().replace(b'\n',b' ')).decode())" "$CORPUS")"
adb logcat -c
adb shell am broadcast -a com.shoktuk.shoktukkeyboard.BENCH --ei count "$COUNT" --ei interval "$INTERVAL" --es b64 "$B64" --ez touch $([ "$TOUCH" = 1 ] && echo true || echo false) --ez bitik $([ "$BITIK" = 1 ] && echo true || echo false) --ei tap "$TAP" --ei drift "$DRIFT" -p com.shoktuk.shoktukkeyboard >/dev/null
echo "Typing $COUNT keys, one every ${INTERVAL} ms…"
adb logcat -s TypingBench:V | awk '{print} /run finished/{exit}'
