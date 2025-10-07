import android.content.Context
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.whl.quickjs.wrapper.QuickJSContext

class JSTranscriber_Alphabet(context: Context) {
    private val jsCtx: QuickJSContext = QuickJSContext.create()

    init {
        val jsFile = if (context.keyboardVariant == BitikVariant.SAMAGAN) "transcriber_alphabet_modern.js"
        else "transcriber_alphabet.js"

        val js = context.assets.open(jsFile).bufferedReader(Charsets.UTF_8).use { it.readText() }
        jsCtx.evaluate(js) // load your JS once
    }

    fun getTranscription(text: String): String = call(text)

    private fun call(input: String): String {
        val s = input.escapeJs()
        val code = """
            (function(){
              var t = new Transcrptiber_Old();
              return t.GetTranscription("$s");
            })();
        """.trimIndent()
        return jsCtx.evaluate(code)?.toString() ?: ""
    }

    fun close() {
        jsCtx.destroy()
    }

    private fun String.escapeJs(): String = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
}