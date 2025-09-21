package com.shoktuk.shoktukkeyboard.project.data

enum class BitikVariant(val id: String) {
    CLASSIC("settings_keyboardVariant_Classic"), SAMAGAN("settings_keyboardVariant_Modern");

    companion object {
        const val KEY = "keyboard_variant"
    }
}

enum class BitikDialect(val id: String) {
    Altay("Алтай"), Orkon("Оркон");

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

enum class ESH_Letter_Variant(val id: String) {
    Default("𐱁"), Second("𐰿");

    companion object {
        const val KEY = "ESH_Letter_Variant"
    }
}

enum class Kirilisa_Status(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Kirilisa_Status"
    }
}

enum class Latin_Status(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Latin_Status"
    }
}

enum class Vibrations(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Vibrations"
    }
}

enum class Sounds(val id: String) {
    Off("Өчүк"), On("Жанык");

    companion object {
        const val KEY = "Sounds"
    }
}

enum class WritingSystem(val id: String) {
    Bitik("Bitik"), Latin("latin"), Kiril("kiril");

    companion object {
        const val KEY = "WritingSystem"
    }
}