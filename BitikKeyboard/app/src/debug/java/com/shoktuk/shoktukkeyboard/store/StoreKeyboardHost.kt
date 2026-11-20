package com.shoktuk.shoktukkeyboard.store

import Language
import LocalizationManager
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.Selection
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.shoktuk.shoktukkeyboard.keyboard.KeyView
import com.shoktuk.shoktukkeyboard.keyboard.KeyboardMode
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService
import com.shoktuk.shoktukkeyboard.keyboard.TopRowBuilder_Old
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.Coloring
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.Arabic_Status
import com.shoktuk.shoktukkeyboard.project.data.Kirilisa_Status
import com.shoktuk.shoktukkeyboard.project.data.Latin_Status
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.arabicStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bottomOffset
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.coloring
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.enVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.suggestionTranscriptionScript
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.kirilisaStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinStatus
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/*
 * The same idea as the iOS store tooling: the REAL keyboard (every key, the suggestion bar, the settings / emoji / savables
 * pages) is built in the app itself, with no input method running and no adb driving: `StoreKeyboardService` is the keyboard's
 * service created by hand, with a made-up text field. A card takes a picture of it (one card after the other, because the
 * keyboard keeps its state in globals) and shows that.
 */

/** What a card shows. The Android counterpart of the iOS `StoreKeyboardConfig` (same names where there is the same thing). */
data class StoreKeyboardConfig(
    val alphabet: WritingSystem = WritingSystem.Bitik,
    val panel: KeyboardMode = KeyboardMode.Main,
    val typed: String? = "",
    /** A word in the field that is not shown (typed = null): the suggestion bar still offers its suggestions, as on iOS. */
    val word: String? = null,
    /** "Auto Bitik": the keyboard converts what is typed to Bitik. */
    val bitikMode: Boolean = false,
    val letterTranscription: Boolean = true,
    val coloring: Boolean = true,
    val shift: Boolean = false,
    val enCut: Boolean = false,
    val wordSeparator: WordSeparator = WordSeparator.NoSpace,
    /** The word the glide trail draws over the keys. */
    val glide: String? = null,
    val variant: BitikVariant = BitikVariant.Modern,
    /** The store language (en, ky_kiril, tr): the keyboard's own texts (settings panel, hints) follow it. A card fills this in itself. */
    val language: String = "en",
    /** The card points at this row of keys with an arrow at its left edge (measured on the real keyboard). */
    val arrowTarget: StoreArrowTarget? = null,
)

/** Which row of keys an arrow points at. */
enum class StoreArrowTarget { LastLetterRow, BottomRow }

/** The keyboard as a picture, plus where each letter key is (pixels of the picture) for the glide trail and the rings. */
class StoreKeyboardImage(val bitmap: ImageBitmap, val keys: Map<String, Offset>, val rows: Map<StoreArrowTarget, Float> = emptyMap(), val gear: Offset? = null)

/** The keyboard's service, made by hand: it is given the app as its context and a text field that lives in memory. */
class StoreKeyboardService : MyKeyboardService() {
    private var connection: BaseInputConnection? = null
    var onRoot: ((View) -> Unit)? = null

    fun attach(base: Context, field: View, text: String) {
        attachBaseContext(base)
        connection = BaseInputConnection(field, true).also { ic ->
            ic.editable?.let { e -> e.replace(0, e.length, text); Selection.setSelection(e, e.length) }
        }
        lifecycleOwner.onCreate()
        lifecycleOwner.onResume()
    }

    override fun getCurrentInputConnection(): InputConnection? = connection
    override fun getCurrentInputEditorInfo(): EditorInfo = EditorInfo()
    override fun showInputView(view: View) { onRoot?.invoke(view) }
}

object StoreKeyboardSnapshotter {
    private val lock = Mutex()

    /** True while a keyboard picture is being made (an export waits for this to be false). */
    val busy: Boolean get() = lock.isLocked

    /** Why the last picture could not be made (shown on the card in a preview, where the native Bitik library may be missing). */
    @Volatile var lastError: String? = null
    private val cache = HashMap<StoreKeyboardConfig, StoreKeyboardImage>()

    suspend fun snapshot(context: Context, config: StoreKeyboardConfig): StoreKeyboardImage? =
        lock.withLock {
            cache[config] ?: withContext(Dispatchers.Main) { render(context, config) }?.also { cache[config] = it }
        }

    /** The activity behind a context, if there is one (a Compose preview in Android Studio has none). */
    private fun activityOf(context: Context): Activity? {
        var c: Context? = context
        while (c is android.content.ContextWrapper) { if (c is Activity) return c; c = c.baseContext }
        return null
    }

    /** The error with the first few lines of where it happened: what a preview shows instead of a keyboard. */
    private fun describe(e: Throwable): String =
        e.toString() + "\n" + e.stackTrace.take(8).joinToString("\n") { "  at ${it.className.substringAfterLast('.')}.${it.methodName}:${it.lineNumber}" }

    private class Rig(val host: FrameLayout, val service: StoreKeyboardService, val content: ViewGroup?, val width: Int) {
        fun release() { content?.removeView(host) }
    }

    /** Applies the card's settings, creates the keyboard's service by hand and builds the keyboard into a host view. */
    private fun build(context: Context, config: StoreKeyboardConfig): Rig {
        val activity = activityOf(context)
        val app = context.applicationContext ?: context

        // The settings this card shows (debug builds only: these are the app's own saved settings).
        app.latinStatus = Latin_Status.On
        app.kirilisaStatus = Kirilisa_Status.On
        app.arabicStatus = Arabic_Status.On
        app.writingSystem = config.alphabet
        app.keyboardVariant = config.variant
        app.letterTranscription = if (config.letterTranscription) LetterTranscription.On else LetterTranscription.Off
        app.coloring = if (config.coloring) Coloring.On else Coloring.Off
        app.wordSeparator = config.wordSeparator
        // The emoji panel's "most used" row: a fixed, friendly set instead of whatever this device happened to use.
        app.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE).edit()
            .putString("emojiRecents.v1", org.json.JSONArray(listOf("😄", "😎", "🤩", "😍", "💪", "🥹", "👍", "❤️", "🎉", "🔥", "🙏", "😊")).toString())
            .apply()
        // The small spelling under the Bitik suggestions: Cyrillic in the Kyrgyz banner, Latin in the others (as on iOS).
        app.suggestionTranscriptionScript = if (config.language.startsWith("ky")) "kiril" else "latin"
        app.enVariant = if (config.enCut) EN_Letter_Variant.Second else EN_Letter_Variant.Default
        app.bottomOffset = 0
        KeyboardTheme.invalidateColorCache()
        Language.entries.find { it.code == config.language }
            ?.let { LocalizationManager.currentLanguage = it }

        val width = context.resources.displayMetrics.widthPixels
        val host = FrameLayout(context)
        val field = View(context)
        val service = StoreKeyboardService()
        service.attach(context, field, config.typed?.takeIf { it.isNotEmpty() } ?: config.word.orEmpty())
        MyKeyboardService.context = service
        MyKeyboardService.keyboardMode = config.panel
        MyKeyboardService.isCaps = config.shift
        MyKeyboardService.isAutoWriteBitikMode = config.bitikMode
        MyKeyboardService.bottomPadding = 0

        service.onRoot = { root ->
            (root.parent as? ViewGroup)?.removeView(root)
            host.removeAllViews()
            host.addView(root, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        }
        // On screen (behind the banner), so the views attach, the suggestion bar's posted work runs and blurs draw.
        // (A preview has no window: the keyboard is still drawn, but without the suggestion bar's background work.)
        val content = activity?.findViewById<ViewGroup>(android.R.id.content)
        content?.addView(host, 0, FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT))
        try {
            service.reloadKeyboard()
        } catch (e: Throwable) {
            content?.removeView(host)
            throw e
        }
        return Rig(host, service, content, width)
    }

    private fun draw(rig: Rig): StoreKeyboardImage? {
        val host = rig.host
        host.measure(View.MeasureSpec.makeMeasureSpec(rig.width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        host.layout(0, 0, host.measuredWidth, host.measuredHeight)
        if (host.measuredHeight <= 0) return null
        val bitmap = Bitmap.createBitmap(host.measuredWidth, host.measuredHeight, Bitmap.Config.ARGB_8888)
        host.draw(Canvas(bitmap))
        return StoreKeyboardImage(bitmap.asImageBitmap(), letterCentres(host), rowCentres(host), gearCentre(host))
    }

    private suspend fun render(context: Context, config: StoreKeyboardConfig): StoreKeyboardImage? {
        val rig = try { build(context, config) } catch (e: Throwable) { lastError = describe(e); return null }
        try {
            // The suggestion bar asks for the word before the cursor and fills in on a background thread: give it time, twice.
            // (Nothing to look up for an empty field or a panel, so those are quick: the cards are made one after the other.)
            val hasWord = (!config.typed.isNullOrEmpty() || !config.word.isNullOrEmpty()) && config.panel == KeyboardMode.Main
            repeat(if (hasWord) 3 else 1) {
                delay(if (hasWord) 300 else 150)
                TopRowBuilder_Old.onTypedListener.invoke()
            }
            delay(if (hasWord) 500 else 150)
            return draw(rig)
        } catch (e: Throwable) {
            lastError = describe(e)
            return null
        } finally {
            rig.release()
        }
    }

    /**
     * The middle (pixels, from the top) of the lowest row of letter keys (shift, delete) and of the bottom row (12 / emoji, space bar).
     * The bottom row's keys are not letter keys, so its middle is one row step (the distance between the last two letter rows) lower.
     */
    private fun rowCentres(host: View): Map<StoreArrowTarget, Float> {
        val mids = ArrayList<Float>()
        fun walk(v: View, dy: Float) {
            if (v is KeyView) {
                if (v.width <= 0 || v.height <= 0) return
                val lower = v.key.lowercase
                if (lower.isNotEmpty() && lower.codePointCount(0, lower.length) == 1) mids += dy + v.height / 2f
            } else if (v is ViewGroup) {
                for (i in 0 until v.childCount) { val c = v.getChildAt(i); walk(c, dy + c.top) }
            }
        }
        walk(host, 0f)
        // One value per row (keys of a row differ by a pixel or two).
        val rows = mids.sorted().fold(ArrayList<Float>()) { acc, m -> if (acc.isEmpty() || m - acc.last() > 8f) acc += m; acc }
        if (rows.isEmpty()) return emptyMap()
        val last = rows.last()
        return buildMap {
            put(StoreArrowTarget.LastLetterRow, last)
            if (rows.size >= 2) put(StoreArrowTarget.BottomRow, last + (last - rows[rows.size - 2]))
        }
    }

    /** The middle (pixels) of the settings gear (⚙) in the top bar, or null when the bar does not show it. */
    private fun gearCentre(host: View): Offset? {
        var found: Offset? = null
        fun walk(v: View, dx: Float, dy: Float) {
            if (v is android.widget.TextView && v.text?.toString() == "⚙" && v.width > 0 && v.isShown) {
                found = Offset(dx + v.width / 2f, dy + v.height / 2f)
            } else if (v is ViewGroup) {
                for (i in 0 until v.childCount) { val c = v.getChildAt(i); walk(c, dx + c.left, dy + c.top) }
            }
        }
        walk(host, 0f, 0f)
        return found
    }

    /** Every letter key with its centre in the host's pixels (a Bitik letter is two UTF-16 units, so the key is its text). */
    private fun letterCentres(host: View): Map<String, Offset> {
        val out = HashMap<String, Offset>()
        fun walk(v: View, dx: Float, dy: Float) {
            if (v is KeyView) {
                val lower = v.key.lowercase
                if (lower.isNotEmpty() && lower.codePointCount(0, lower.length) == 1 && v.width > 0) out[lower] = Offset(dx + v.width / 2f, dy + v.height / 2f)
            } else if (v is ViewGroup) {
                for (i in 0 until v.childCount) { val c = v.getChildAt(i); walk(c, dx + c.left, dy + c.top) }
            }
        }
        walk(host, 0f, 0f)
        return out
    }
}
