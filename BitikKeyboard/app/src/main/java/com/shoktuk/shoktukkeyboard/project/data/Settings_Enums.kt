package com.shoktuk.shoktukkeyboard.project.data

enum class BitikVariant(val id: String) {
    CLASSIC("keyboardVariant_Classic"), Modern("keyboardVariant_Standart"), SAMAGAN("keyboardVariant_Modern");

    companion object {
        const val KEY = "keyboard_variant"
    }
}

enum class BitikDialect(val id: String) {
    Altay("keyboardDialect_Altay"), Orkon("keyboardDialect_Orhon");

    companion object {
        const val KEY = "BitikDialect"
    }
}

enum class TextTranscription(val id: String) {
    On("Жанык"), Off("Өчүк");

    companion object {
        const val KEY = "text_transcription"
    }
}

enum class LetterTranscription(val id: String) {
    On("Жанык"), Off("Өчүк");

    companion object {
        const val KEY = "letter_transcription"
    }
}

enum class WordSeparator(val id: String) {
    Off("Жок"), NoSpace("⁚"), SpaceBefore("{ }⁚"), ArroundSpace("{ }⁚{ }");

    companion object {
        const val KEY = "WordSeparator"
    }
}

enum class AJ_Letter_Variant(val id: String) {
    Default("𐰳"), Ay("𐰖");

    companion object {
        const val KEY = "AJ_Letter_Variant"
    }
}

enum class ANG_Letter_Variant(val id: String) {
    Off("Off"), On("On");

    companion object {
        const val KEY = "ANG_Letter_Variant"
    }
}

enum class EB_Letter_Variant(val id: String) {
    Default("𐰌"), Second("𐰋");

    companion object {
        const val KEY = "EB_Letter_Variant"
    }
}

enum class EN_Letter_Variant(val id: String) {
    Default("𐰤"), Second("𐰥");

    companion object {
        const val KEY = "EN_Letter_Variant"
    }
}

enum class AS_Letter_Variant(val id: String) {
    Default("𐰽"), Second("𐱂");

    companion object {
        const val KEY = "AS_variant"
    }
}

enum class EK_Letter_Variant(val id: String) {
    Default("𐰚"), Second("𐰛");

    companion object {
        const val KEY = "EK_Letter_Variant"
    }
}

enum class ESH_Letter_Variant(val id: String) {
    Default("𐱁"), Second("𐰿");

    companion object {
        const val KEY = "ESH_Letter_Variant"
    }
}

enum class Latin_Status(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Latin_Status"
    }
}

enum class Latin_Variant(val id: String) {
    minimal("latinAlphabet_Kyrgyz"), full("latinAlphabet_Full");

    companion object {
        const val KEY = "Latin_Variant"
    }
}

enum class Latin_ZH(val id: String) {
    j("j"), c("c");

    companion object {
        const val KEY = "Latin_ZH"
    }
}

enum class Arabic_Status(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Arabic_Status"
    }
}

enum class Kirilisa_Status(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Kirilisa_Status"
    }
}

enum class Vibrations(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Vibrations"
    }
}

enum class Coloring(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Coloring"
    }
}

enum class HoldabilityColoring(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "HoldabilityColoring"
    }
}

enum class Sounds(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Sounds"
    }
}

enum class WritingSystem(val id: String) {
    Bitik("Bitik"), Latin("latin"), Arab("arabic"), Kiril("kiril");

    companion object {
        const val KEY = "WritingSystem"
    }
}

enum class NavBarPaddingSolution(val id: String) {
    Solution_AllEnabled("№1"), Solution_Enable_1("№2"), Solution_Enable_2("№3"), Solution_Enable_Off("№4");

    companion object {
        const val KEY = "NavBarPaddingSolution"
    }
}