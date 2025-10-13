import android.content.Context
import android.util.Log
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.QuickJSContext

class JSTranscriber_Alphabet(context: Context) {
    private val jsCtx: QuickJSContext? = try {
        QuickJSLoader.init()

        val ctx = QuickJSContext.create()
        val jsFile = if (context.keyboardVariant == BitikVariant.SAMAGAN) "transcriber_alphabet_modern.js"
        else "transcriber_alphabet.js"

        val js = context.assets.open(jsFile).bufferedReader(Charsets.UTF_8).use { it.readText() }
        ctx.evaluate(js) // load your JS once
        ctx
    } catch (e: Exception) {
        Log.e("JSTranscriber_Alphabet", "Failed to initialize QuickJS context. Transcriber will be disabled.", e)
        null
    }

    fun getTranscription(text: String): String = call(text)

    private fun call(input: String): String {
        return try {
            val safeInput = input
                .replace("\\", "\\\\")  // escape backslashes
                .replace("\"", "\\\"")  // escape double quotes
                .replace("`", "\\`")    // escape backticks
                .replace("\n", "\\n")   // escape newlines
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
            // If JS context or evaluation fails, fall back safely
            "Error: ${e.message ?: "Unknown error"}"
        }
    }

    fun close() {
        jsCtx?.destroy()
    }

    private fun String.escapeJs(): String = replace("\\", "\\").replace("\"", "\"").replace("\n", "\n").replace("\r", "\r")
}
