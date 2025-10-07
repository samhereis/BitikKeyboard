import android.content.Context
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.whl.quickjs.wrapper.QuickJSContext

class JSTranscriber(context: Context) {
    private val ctx: QuickJSContext = QuickJSContext.create()

    init {
        val jsFile = if (context.keyboardVariant == BitikVariant.CLASSIC)
            "transcriber_old.js" else "transcriber_modern.js"
        val js = context.assets.open(jsFile).bufferedReader(Charsets.UTF_8).use { it.readText() }
        ctx.evaluate(js)
    }

    fun getTranscription(text: String): String {
        val s = text.escapeJs()
        val call = """
            (function(){
              var t = new CorrentText_Old();
              return t.GetTranscription("$s");
            })();
        """.trimIndent()
        return ctx.evaluate(call)?.toString() ?: ""
    }

    fun getTranscription_Alternative(text: String): String {
        val s = text.escapeJs()
        val call = """
            (function(){
              var t = new CorrentText_Old();
              return t.GetTranscription_Alternative("$s");
            })();
        """.trimIndent()
        return ctx.evaluate(call)?.toString() ?: ""
    }

    fun close() {
        ctx.destroy()
    }

    private fun String.escapeJs() = replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}