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
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.suggestions.WordSuggestionEngine
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

/** Every Latin spelling each Bitik glyph can have, read from the transcription file of the current variant. */
object BitikReadings {
    private var file = ""
    private var table: Map<String, List<String>> = emptyMap()
    private val entry = Regex("""new TranscriptionEntry\("([^"]+)",\s*"([^"]*)",\s*CharacterType\.\w+(?:,\s*"([^"]*)")?\)""")

    /** Readings of [glyph], already folded with [WordSuggestionEngine.bitikNorm]. */
    fun of(ctx: Context, glyph: String): List<String> {
        val name = if (ctx.keyboardVariant == BitikVariant.CLASSIC) "transcriber_old.js" else "transcriber_modern.js"
        if (name != file) {
            file = name
            val map = HashMap<String, MutableList<String>>()
            try {
                val source = ctx.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }
                for (m in entry.findAll(source)) {
                    val key = m.groupValues[1]
                    for (g in 2..3) {
                        val text = m.groupValues[g]
                        if (text.isEmpty()) continue
                        // Capitals are the letters; a lowercase "a"/"e" in front only hints at an implied vowel.
                        val sb = StringBuilder()
                        for (ch in text) if (ch.isUpperCase()) sb.append(when (ch) { 'I' -> 'ı'; 'İ' -> 'i'; else -> ch.lowercaseChar() })
                        val reading = WordSuggestionEngine.bitikNorm(sb.toString())
                        if (reading.isNotEmpty()) { val list = map.getOrPut(key) { ArrayList() }; if (!list.contains(reading)) list.add(reading) }
                    }
                }
            } catch (_: java.io.IOException) {
            }
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
    private var alphabetTranscriber: JSTranscriber_Alphabet? = null

    private fun separator(): String = when (context.wordSeparator) {
        WordSeparator.NoSpace -> "⁚"
        WordSeparator.SpaceBefore -> "⁚ "
        WordSeparator.ArroundSpace -> " ⁚ "
        WordSeparator.Off -> " "
    }

    /** A thin rule along the bottom of the row, between the cards and the first key row, so a tap meant for a key never hits a card. */
    fun addDivider(row: LinearLayout) {
        val density = row.resources.displayMetrics.density
        val paint = android.graphics.Paint().apply { color = KeyboardTheme.getColor(2).toColorInt(); alpha = 40; strokeWidth = density }
        row.foreground = object : android.graphics.drawable.Drawable() {
            override fun draw(canvas: android.graphics.Canvas) {
                val y = bounds.bottom - density
                canvas.drawLine(bounds.left + 8 * density, y, bounds.right - 8 * density, y, paint)
            }
            override fun setAlpha(a: Int) {}
            override fun setColorFilter(f: android.graphics.ColorFilter?) {}
            @Deprecated("Deprecated in Java") override fun getOpacity() = android.graphics.PixelFormat.TRANSLUCENT
        }
        row.setPadding(row.paddingLeft, row.paddingTop, row.paddingRight, (2 * density).toInt())
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
        val extraSeparators = "·.,⸮⹁:;!?()[]{}\"'"
        WordSuggestionEngine.onShardLoaded = { TopRowBuilder_Old.onTypedListener.invoke() }
        WordSuggestionEngine.configure(context, cyrillic = false, zh = with(com.shoktuk.shoktukkeyboard.project.data.SettingsManager) { context.latinZH.id },
            ch = with(com.shoktuk.shoktukkeyboard.project.data.SettingsManager) { context.latinCH.id },
            ng = with(com.shoktuk.shoktukkeyboard.project.data.SettingsManager) { context.latinNG.id },
            o = with(com.shoktuk.shoktukkeyboard.project.data.SettingsManager) { context.latinO.id })

        text_Original = ic?.getTextBeforeCursor(100, 0)?.toString().orEmpty()
        val regex = "[^\\p{L}${Regex.escape(extraSeparators)}]+".toRegex()
        val raw = text_Original.split(regex).lastOrNull().orEmpty()
        inputText_LastWord = TranscriptionProccessor().processTranscription_bitik(raw, context)
        InputText_Transcribed = transcriber.getTranscription(inputText_LastWord).orEmpty().ifEmpty { inputText_LastWord }
        InputText_Transcribed_Alt = transcriber.getTranscription_Alternative(inputText_LastWord).orEmpty().ifEmpty { inputText_LastWord }

        container.removeAllViews()
        if (text_Original.isEmpty() || inputText_LastWord.isEmpty()) return

        val density = container.resources.displayMetrics.density
        val textColor = KeyboardTheme.getColor(2).toColorInt()
        val chipColor = KeyboardTheme.getColor(6).toColorInt()
        val bitikFace = KeyboardTheme.bitikTypeface(container.context) ?: Typeface.DEFAULT_BOLD
        val height = container.layoutParams.height.coerceAtLeast(1)

        val strip = LinearLayout(container.context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val scroll = HorizontalScrollView(container.context).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            isHorizontalFadingEdgeEnabled = true
            setFadingEdgeLength((20 * density).toInt())
            addView(strip, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        container.addView(scroll, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        fun label(text: String, sp: Float, bitik: Boolean = false, alpha: Float = 1f) = TextView(container.context).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
            setTextColor(textColor)
            this.alpha = alpha
            gravity = Gravity.CENTER
            maxLines = 1
            includeFontPadding = false
            if (bitik) { typeface = bitikFace; paint.isFakeBoldText = true }
        }

        fun commit(bitik: String) {
            ic ?: return
            ic.beginBatchEdit()
            ic.deleteSurroundingText(raw.length, 0)
            ic.commitText(bitik + separator(), 1)
            ic.endBatchEdit()
            TopRowBuilder_Old.onTypedListener.invoke()
        }

        // 1. The literal Latin reading, as the old read-out showed it: each letter with its other reading above.
        val literal = LinearLayout(container.context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding((8 * density).toInt(), 0, (8 * density).toInt(), 0)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height).apply { marginEnd = (6 * density).toInt() }
            setOnClickListener {
                // The literal card is what is already typed: it only ends the word.
                ic?.commitText(separator(), 1)
                TopRowBuilder_Old.onTypedListener.invoke()
            }
        }
        val fullSp = 16f
        val halfSp = 9f
        InputText_Transcribed.forEachIndexed { i, base ->
            val alt = InputText_Transcribed_Alt.getOrNull(i)
            if (alt != null && alt != base) {
                literal.addView(LinearLayout(container.context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    addView(label(alt.toString(), halfSp, bitik = true, alpha = 0.7f))
                    addView(label(base.toString(), fullSp, bitik = true))
                })
            } else literal.addView(label(base.toString(), fullSp, bitik = true))
        }
        strip.addView(literal)

        // 2. Cards: the Bitik word with its Latin spelling below.
        val glyphs = ArrayList<String>()
        var i = 0
        while (i < inputText_LastWord.length) {
            val cp = inputText_LastWord.codePointAt(i)
            glyphs.add(String(Character.toChars(cp)))
            i += Character.charCount(cp)
        }
        val readings = glyphs.map { BitikReadings.of(context, it) }
        if (readings.isEmpty() || readings.any { it.isEmpty() }) return
        val literalLatin = InputText_Transcribed.lowercase()
        val words = WordSuggestionEngine.bitikCompletions(readings, 12)
        val transcriberAlphabet = alphabetTranscriber ?: JSTranscriber_Alphabet(context).also { alphabetTranscriber = it }
        for (w in words) {
            if (w.latin.lowercase() == literalLatin) continue
            val bitik = TranscriptionProccessor().processTranscription_alphabet(
                transcriberAlphabet.getTranscription(w.latin).orEmpty().ifEmpty { w.latin }, context)
            strip.addView(LinearLayout(container.context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 48f; setColor(chipColor) }
                setPadding((12 * density).toInt(), 0, (12 * density).toInt(), 0)
                addView(label(bitik, fullSp, bitik = true))
                addView(label(w.latin, halfSp, alpha = 0.6f))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (height * 0.94f).toInt()).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    marginEnd = (6 * density).toInt()
                }
                setOnClickListener { commit(bitik) }
            })
        }
    }
}
