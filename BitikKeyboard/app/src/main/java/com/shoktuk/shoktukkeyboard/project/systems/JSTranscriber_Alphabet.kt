import android.content.Context
import android.util.Log
import com.shoktuk.shoktukkeyboard.project.data.ANG_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.Latin_CH
import com.shoktuk.shoktukkeyboard.project.data.Latin_ZH
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.angVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinCH
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinZH
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.QuickJSContext
import java.text.Normalizer

class JSTranscriber_Alphabet(context: Context) {
    private val appContext = context.applicationContext

    private val jsCtx: QuickJSContext? = try {
        QuickJSLoader.init()

        val ctx = QuickJSContext.create()
        val jsFile = if (context.keyboardVariant == BitikVariant.SAMAGAN) "transcriber_alphabet_modern.js"
        else "transcriber_alphabet.js"

        var js = context.assets.open(jsFile).bufferedReader(Charsets.UTF_8).use { it.readText() }

        if (context.bitikDialect == BitikDialect.Orkon) {
            if (context.angVariant == ANG_Letter_Variant.Off) {
                var normalizedSource = Normalizer.normalize(js, Normalizer.Form.NFC)

                var old = """new TranscriptionEntry("ң", "𐰬", "𐰭", CharacterType.Consonant),"""
                var new = """new TranscriptionEntry("ң", "𐰭", "𐰭", CharacterType.Consonant_Univ),"""
                js = normalizedSource.replace(old, new)

                old = """new TranscriptionEntry("𐰬", "aÑ", CharacterType.HardConsonant),"""
                new = ""
                js = js.replace(old, new)
            }
        }

        ctx.evaluate(js)
        ctx
    } catch (e: Exception) {
        Log.e("JSTranscriber_Alphabet", "Failed to initialize QuickJS context. Transcriber will be disabled.", e)
        null
    }

    fun getTranscription(text: String): String {
        var input = text
        if (appContext.latinZH == Latin_ZH.j && appContext.latinCH == Latin_CH.c) {
            input = input.replace("c", "ç").replace("C", "Ç")
        }
        return call(input)
    }

    private fun call(input: String): String {
        return try {
            val safeInput = input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("`", "\\`")
                .replace("\n", "\\n")
                .replace("\r", "\\r")

            val code = """
            (function(){
              try {
                var t = new Transcrptiber_Old();
                return t.GetTranscription("$safeInput");
              } catch (e) {
                return "Error: " + e.message;
              }
            })();
        """.trimIndent()

            jsCtx?.evaluate(code)?.toString() ?: input
        } catch (e: Exception) {

            "Error: ${e.message ?: "Unknown error"}"
        }
    }

    fun close() {
        jsCtx?.destroy()
    }

    private fun String.escapeJs(): String = replace("\\", "\\").replace("\"", "\"").replace("\n", "\n").replace("\r", "\r")
}
