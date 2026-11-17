package com.shoktuk.shoktukkeyboard.keyboard

import JSTranscriber
import JSTranscriber_Alphabet
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService.Companion.context
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.InputText_Transcribed
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.InputText_Transcribed_Alt
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.inputText_LastWord
import com.shoktuk.shoktukkeyboard.keyboard.onKeyPressed.text_Original
import com.shoktuk.shoktukkeyboard.project.data.AJ_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.ajVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.angVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionTranscriptionScript
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.suggestions.SuggestionViews
import com.shoktuk.shoktukkeyboard.suggestions.SuggestionWorker
import com.shoktuk.shoktukkeyboard.suggestions.WordSuggestionEngine
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

/** Every Latin spelling each Bitik glyph can have, read from the transcription file of the current variant. */
object BitikReadings {
    private var file = ""
    private var table: Map<String, List<String>> = emptyMap()
    private val entry = Regex("""new TranscriptionEntry\("([^"]+)",\s*"([^"]*)",\s*CharacterType\.\w+(?:,\s*"([^"]*)")?\)""")

    /** On the Modern keyboard 𐰩 is the U key (the tables list it as NÇ) and 𐰆 is only O (the tables give U / O). */
    fun forVariant(source: String, ctx: Context): String =
        if (ctx.keyboardVariant != BitikVariant.SAMAGAN) source
        else source
            .replace("""new TranscriptionEntry("𐰩", "NÇ", CharacterType.Character),""", """new TranscriptionEntry("𐰩", "U", CharacterType.HardVowel),""")
            .replace("""new TranscriptionEntry("𐰆", "U", CharacterType.HardVowel, "O"),""", """new TranscriptionEntry("𐰆", "O", CharacterType.HardVowel),""")

    /** Cyrillic letter for each capital the transcription tables use as a Latin letter. */
    private val cyrillicOf = mapOf(
        'A' to 'а', 'B' to 'б', 'D' to 'д', 'E' to 'е', 'G' to 'г', 'Ğ' to 'г', 'I' to 'ы', 'İ' to 'и', 'J' to 'ж',
        'K' to 'к', 'Q' to 'к', 'L' to 'л', 'M' to 'м', 'N' to 'н', 'Ñ' to 'ң', 'O' to 'о', 'Ö' to 'ө', 'P' to 'п',
        'R' to 'р', 'S' to 'с', 'Ş' to 'ш', 'T' to 'т', 'U' to 'у', 'Ü' to 'ү', 'Y' to 'й', 'Z' to 'з', 'Ç' to 'ч',
    )

    /** Every Cyrillic spelling [glyph] can have, read from the transcription file of the current variant. */
    fun of(ctx: Context, glyph: String): List<String> {
        val name = if (ctx.keyboardVariant == BitikVariant.CLASSIC) "transcriber_old.js" else "transcriber_modern.js"
        val swapsAJ = ctx.keyboardVariant != BitikVariant.CLASSIC && ctx.ajVariant == AJ_Letter_Variant.Jaa
        val tableKey = name + ctx.keyboardVariant.name + swapsAJ
        if (tableKey != file) {
            file = tableKey
            val map = HashMap<String, MutableList<String>>()
            try {
                val source = forVariant(ctx.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }, ctx)
                for (m in entry.findAll(source)) {
                    val key = m.groupValues[1]
                    for (g in 2..3) {
                        val text = m.groupValues[g]
                        if (text.isEmpty()) continue
                        // Capitals are the letters; a lowercase "a"/"e" in front only hints at an implied vowel.
                        val reading = text.mapNotNull { cyrillicOf[it] }.joinToString("")
                        if (reading.isNotEmpty()) { val list = map.getOrPut(key) { ArrayList() }; if (!list.contains(reading)) list.add(reading) }
                    }
                }
            } catch (_: java.io.IOException) {
            }
            // On the Modern keyboard every key means one thing (its first reading, except 𐰱 which is чы or чи); 𐰆 is only "о".
            if (ctx.keyboardVariant == BitikVariant.SAMAGAN) {
                for (k in map.keys.toList()) map[k] = map[k]!!.take(1).toMutableList()
                map["𐰆"] = mutableListOf("о")
                map["𐰱"] = mutableListOf("чы", "чи")
            }
            // With the "ай" setting 𐰖 is the ж tamga and 𐰗 the й one (as in the Bitik spelling of words).
            if (swapsAJ) { map["𐰖"] = mutableListOf("ж"); map["𐰗"] = mutableListOf("й") }
            table = map
        }
        return table[glyph].orEmpty()
    }
}

/**
 * The suggestion strip of the Bitik keyboard. The first item is the Latin reading of what is typed (the old read-out,
 * each letter with its other reading above); after it come cards with the Bitik word and its Latin spelling below.
 */
object BitikSuggestionStrip {
    /** The ★ and ⚙ buttons of the current Bitik row: hidden while the strip holds more than three items. */
    var sideButtons: List<View> = emptyList()

    private var alphabetTranscriber: JSTranscriber_Alphabet? = null
    private var alphabetKey = ""

    fun separator(): String = when (context.wordSeparator) {
        WordSeparator.NoSpace -> "⁚"
        WordSeparator.SpaceBefore -> "⁚ "
        WordSeparator.ArroundSpace -> " ⁚ "
        WordSeparator.Off -> " "
    }

    /** The Bitik spelling of a Cyrillic word as the keyboard writes it. Call on the suggestion thread only (QuickJS). */
    fun bitikSpelling(ctx: Context, cyrillic: String): String {
        val transcriber = alphabetTranscriber?.takeIf { alphabetKey == alphabetKeyNow(ctx) }
            ?: JSTranscriber_Alphabet(ctx).also { alphabetTranscriber = it; alphabetKey = alphabetKeyNow(ctx) }
        return TranscriptionProccessor().processTranscription_alphabet(transcriber.getTranscription(cyrillic).orEmpty().ifEmpty { cyrillic }, ctx)
    }

    /** A container for the strip; [update] fills it. */
    fun createContainer(service: android.inputmethodservice.InputMethodService, buttonHeight: Int): FrameLayout = FrameLayout(service).apply {
        clipChildren = true
        clipToPadding = true
        layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
            marginStart = MyKeyboardService.buttonMargin
            marginEnd = MyKeyboardService.buttonMargin
        }
    }

    fun update(ic: InputConnection?, transcriber: JSTranscriber, container: FrameLayout) {
        val zh: String; val ch: String; val ng: String; val o: String
        with(com.shoktuk.shoktukkeyboard.project.data.SettingsManager) { zh = context.latinZH.id; ch = context.latinCH.id; ng = context.latinNG.id; o = context.latinO.id }

        SuggestionWorker.cancel()
        val raw = LastWord.extract(ic)
        inputText_LastWord = TranscriptionProccessor().processTranscription_bitik(raw, context)
        InputText_Transcribed = transcriber.getTranscription(inputText_LastWord).orEmpty().ifEmpty { inputText_LastWord }
        InputText_Transcribed_Alt = transcriber.getTranscription_Alternative(inputText_LastWord).orEmpty().ifEmpty { inputText_LastWord }

        if (text_Original.isEmpty() || inputText_LastWord.isEmpty() || !MyKeyboardService.canSuggest()) {
            container.removeAllViews()
            sideButtons.forEach { it.visibility = View.VISIBLE }
            return
        }

        val height = container.layoutParams.height.coerceAtLeast(1)
        val cardHeight = (height * 0.94f).toInt()
        val sp = KeyboardTheme.getSystemButtonStyle(container.context).textSizeSp

        fun commit(bitik: String) {
            ic ?: return
            ic.beginBatchEdit()
            ic.deleteSurroundingText(MyKeyboardService.safeWordLength(ic, raw.length), 0)
            ic.commitText(bitik + separator(), 1)
            ic.endBatchEdit()
            MyKeyboardService.markAutoSpace(ic)
            TopRowBuilder_Old.onTypedListener.invoke()
        }

        val glyphs = ArrayList<String>()
        var i = 0
        while (i < raw.length) {
            val cp = raw.codePointAt(i)
            glyphs.add(String(Character.toChars(cp)))
            i += Character.charCount(cp)
        }
        val ctx = context
        val typed = InputText_Transcribed
        val typedAlt = InputText_Transcribed_Alt
        val showTranscription = ctx.suggestionTranscription(WritingSystem.Bitik)
        val cyrillicBelow = ctx.suggestionTranscriptionScript == "kiril"
        // The search and the Bitik spelling of each word run on the suggestion thread. The strip is rebuilt in one go when
        // they are done, so what is on screen (and the side buttons) stay as they are meanwhile instead of flashing empty.
        SuggestionWorker.submit({
            WordSuggestionEngine.configure(ctx, cyrillic = true, zh = zh, ch = ch, ng = ng, o = o)
            val readings = glyphs.map { BitikReadings.of(ctx, it) }
            if (readings.isEmpty() || readings.any { it.isEmpty() }) return@submit emptyList()
            val words = WordSuggestionEngine.bitikCompletions(readings, 12)
            // The alphabet transcriber (QuickJS) is created and used on this thread only.
            val transcriberAlphabet = alphabetTranscriber?.takeIf { alphabetKey == alphabetKeyNow(ctx) }
                ?: JSTranscriber_Alphabet(ctx).also { alphabetTranscriber = it; alphabetKey = alphabetKeyNow(ctx) }
            words.map { w ->
                w to TranscriptionProccessor().processTranscription_alphabet(transcriberAlphabet.getTranscription(w.cyr).orEmpty().ifEmpty { w.cyr }, ctx)
            }
        }) { cards ->
            container.removeAllViews()
            val strip = SuggestionViews.strip(container.context)
            container.addView(strip.scroll, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

            // 1. The literal Latin reading, as the old read-out showed it: each letter with its other reading above.
            val literal = LinearLayout(container.context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
            typed.forEachIndexed { index, base ->
                val alt = typedAlt.getOrNull(index)
                if (alt != null && alt != base) {
                    // Two floors, both at half size: the other reading above, the main one below.
                    literal.addView(LinearLayout(container.context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        addView(SuggestionViews.text(container.context, alt.toString(), sp / 2f, bitik = true))
                        addView(SuggestionViews.text(container.context, base.toString(), sp / 2f, bitik = true))
                    })
                } else literal.addView(SuggestionViews.text(container.context, base.toString(), sp, bitik = true))
            }
            // What is already typed: tapping only ends the word.
            strip.row.addView(SuggestionViews.card(container.context, cardHeight, literal, null) {
                ic?.commitText(separator(), 1)
                TopRowBuilder_Old.onTypedListener.invoke()
            })

            // 2. Cards: the Bitik word with its Latin spelling below.
            for ((w, bitik) in cards) {
                strip.row.addView(SuggestionViews.wordCard(container.context, cardHeight, bitik, sp, bitik = true, transcription = if (showTranscription) (if (cyrillicBelow) w.cyr else w.latin) else null) { com.shoktuk.shoktukkeyboard.suggestions.LearnedWords.learn(w.cyr); commit(bitik) })
            }
            sideButtons.forEach { it.visibility = if (strip.row.childCount <= 3) View.VISIBLE else View.GONE }
        }
    }

    // The alphabet file is patched per variant, dialect and ang setting, so a changed setting needs a new transcriber.
    private fun alphabetKeyNow(ctx: Context) = "${ctx.keyboardVariant}${ctx.bitikDialect}${ctx.angVariant}"
}
