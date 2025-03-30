package com.shoktuk.shoktukkeyboard.keyboard

import androidx.lifecycle.ViewModel

// 0 - Strong Character
// 1 - Hard Consonant
// 2 - Soft Consonant
// 3 - Strong Consonant
// 4 - Strong Vowel
// 5 - Flexible Vowel

data class TranscriptionEntry(
    val symbol: String, val latinTranscription: String, val type: Int, val vowel: String? = null, val vowelPosition: Int? = 0, val forceAddPreviousVowel: Boolean = false
)

class CorrectText : ViewModel() {
    private val transcriptionList = listOf(
        TranscriptionEntry("", "", -1, ""),

        // First Row
        TranscriptionEntry("𐰶", "QƗ", 0, "Ɨ", 1),
        TranscriptionEntry("𐰬", "aÑ", 1),
        TranscriptionEntry("𐰧", "aÑ", 1),
        TranscriptionEntry("𐰁", "A", 4),
        TranscriptionEntry("𐰀", "A", 4),
        TranscriptionEntry("𐰺", "aR", 1),
        TranscriptionEntry("𐱄", "aT", 1),
        TranscriptionEntry("𐱇", "OT", 0, "O"),
        TranscriptionEntry("𐰖", "aY", 1),
        TranscriptionEntry("𐰗", "AY", 0, "A"),
        TranscriptionEntry("𐰩", "U", 4),
        TranscriptionEntry("𐰃", "Ï", 4),
        TranscriptionEntry("𐰆", "O", 4),
        TranscriptionEntry("𐰯", "P", 3),
        TranscriptionEntry("𐰊", "F", 3),

        // First Row - Shift
        TranscriptionEntry("𐰷", "ƗQ", 0, "Ɨ"),
        TranscriptionEntry("𐰭", "eÑ", 2),
        TranscriptionEntry("𐰮", "eÑ", 2),
        TranscriptionEntry("𐰅", "E", 5),
        TranscriptionEntry("𐰂", "E", 5),
        TranscriptionEntry("𐰼", "eR", 2),
        TranscriptionEntry("𐱅", "eT", 2),
        TranscriptionEntry("𐰘", "eY", 2),
        TranscriptionEntry("𐰇", "Ü", 5),
        TranscriptionEntry("𐰄", "İ", 5),
        TranscriptionEntry("𐰈", "Ö", 5),

        // Second Row
        TranscriptionEntry("𐰹", "OQ", 0, "O"),
        TranscriptionEntry("𐱂", "aS", 1),
        TranscriptionEntry("𐰽", "SU", 0, "U"),
        TranscriptionEntry("𐰑", "aD", 1),
        TranscriptionEntry("𐰒", "aD", 1),
        TranscriptionEntry("𐰦", "NT", 0),
        TranscriptionEntry("𐰍", "aǦ", 1),
        TranscriptionEntry("𐰎", "aǦ", 1),
        TranscriptionEntry("𐱈", "RT", 0),
        TranscriptionEntry("𐰳", "aJ", 1),
        TranscriptionEntry("𐰴", "aQ", 1),
        TranscriptionEntry("𐰛", "H", 3),
        TranscriptionEntry("𐰞", "aL", 1),
        TranscriptionEntry("𐰟", "aL", 1),

        // Second Row - Shift
        TranscriptionEntry("𐰸", "UQ", 0, "U"),
        TranscriptionEntry("𐰾", "eS", 2),
        TranscriptionEntry("𐰓", "eD", 2),
        TranscriptionEntry("𐰨", "NÇ", 0),
        TranscriptionEntry("𐰏", "eG", 2),
        TranscriptionEntry("𐰐", "eG", 2),
        TranscriptionEntry("𐰡", "LT", 0),
        TranscriptionEntry("𐰙", "eJ", 2),
        TranscriptionEntry("𐰚", "eK", 2),
        TranscriptionEntry("𐰠", "eL", 2),

        // Third Row
        TranscriptionEntry("𐰕", "Z", 3),
        TranscriptionEntry("𐱀", "aŞ", 1),
        TranscriptionEntry("𐰲", "Ç", 3),
        TranscriptionEntry("𐰱", "ÇƗ", 0, "Ɨ"),
        TranscriptionEntry("𐰜", "ÜK", 0),
        TranscriptionEntry("𐰉", "aB", 1),
        TranscriptionEntry("𐰋", "V", 3),
        TranscriptionEntry("𐰣", "aN", 1),
        TranscriptionEntry("𐰢", "M", 3),

        // Third Row - Shift
        TranscriptionEntry("𐰿", "eŞ", 2),
        TranscriptionEntry("𐰝", "ÖK", 0),
        TranscriptionEntry("𐰌", "eB", 2),
        TranscriptionEntry("𐰥", "eN", 2)
    )

    fun getTranscription(text: String): String {
        var textTemp = ""
        var index = 0

        var lastVowel = ""
        var lastTransriptionData: TranscriptionEntry = transcriptionList[0]
        var forceUpperCaseVowel = false

        while (index < text.length) {
            val codePoint = text.codePointAt(index)
            val symbol = String(Character.toChars(codePoint))

            if (Character.getType(codePoint).toInt() != Character.FORMAT.toInt()) {
                val transcriptionData = transcriptionList.find { it.symbol == symbol }
                var transriptionString = transcriptionData?.latinTranscription ?: ""

                if (transcriptionData != null) {
                    if (transcriptionData.type == 0) {// 0 - Strong Character
                        if (transcriptionData.vowel != null) {
                            lastVowel = transcriptionData.vowel
                        }
                    }

                    if (transcriptionData.type == 1) {// 1 - Hard Consonant
                        if (lastTransriptionData.type == -1) {
                            transriptionString = transriptionString.replace("a", "")
                            lastVowel = "A"
                            forceUpperCaseVowel = true
                        } else {
                            if (lastVowel.isNotEmpty()) {
                                if (lastTransriptionData.type == 4) {
                                    if (lastTransriptionData.latinTranscription == "A") {
                                        transriptionString = transriptionString.replace("a", "A")
                                    } else {
                                        transriptionString = transriptionString.replace("a", "")
                                    }
                                } else {
                                    if (lastTransriptionData.vowelPosition == 1) {
                                        transriptionString = transriptionString.replace("a", "")
                                    } else {
                                        if (forceUpperCaseVowel) {
                                            transriptionString = transriptionString.replace("a", lastVowel)
                                            forceUpperCaseVowel = false
                                        } else {
                                            transriptionString = transriptionString.replace("a", lastVowel.lowercase())
                                        }
                                    }
                                }
                            } else if (forceUpperCaseVowel) {
                                transriptionString = transriptionString.replace("a", "A")
                                forceUpperCaseVowel = false
                            }
                        }
                    }

                    if (transcriptionData.type == 2) {// 1 - Hard Consonant
                        if (lastTransriptionData.type == -1) {
                            transriptionString = transriptionString.replace("e", "")
                            lastVowel = "E"
                            forceUpperCaseVowel = true
                        } else {
                            if (lastVowel.isNotEmpty()) {
                                if (lastTransriptionData.type == 5) {
                                    if (lastTransriptionData.latinTranscription == "E") {
                                        transriptionString = transriptionString.replace("e", "E")
                                    } else {
                                        transriptionString = transriptionString.replace("e", "")
                                    }
                                } else {
                                    if (lastTransriptionData.vowelPosition == 1) {
                                        transriptionString = transriptionString.replace("e", "")
                                    } else {
                                        if (forceUpperCaseVowel) {
                                            transriptionString = transriptionString.replace("e", lastVowel)
                                            forceUpperCaseVowel = false
                                        } else {
                                            transriptionString = transriptionString.replace("e", lastVowel.lowercase())
                                        }
                                    }
                                }
                            } else if (forceUpperCaseVowel) {
                                transriptionString = transriptionString.replace("e", "E")
                                forceUpperCaseVowel = false
                            }
                        }
                    }

                    if (transcriptionData.type == 3) { // 3 - Strong Consonant
                        if (lastTransriptionData.type == -1) {
                            forceUpperCaseVowel = true
                        }
                    }

                    if (transcriptionData.type == 4) { // 4 - Strong Vowel
                        lastVowel = transcriptionData.latinTranscription
                    }

                    if (transcriptionData.type == 5) { // 5 - Flexible Vowel
                        lastVowel = transcriptionData.latinTranscription
                    }

                    lastTransriptionData = transcriptionData
                }
                textTemp += transriptionString
            }

            index = text.offsetByCodePoints(index, 1)
        }

        return textTemp
    }
}