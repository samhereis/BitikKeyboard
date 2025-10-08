import android.content.Context
import android.util.Log
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.QuickJSContext

class JSTranscriber(context: Context) {
    private val jsCtx: QuickJSContext? = try {
        QuickJSLoader.init()

        val jsCtx = QuickJSContext.create()
        val jsFile = if (context.keyboardVariant == BitikVariant.CLASSIC) "transcriber_old.js" else "transcriber_modern.js"
        val js = context.assets.open(jsFile).bufferedReader(Charsets.UTF_8).use { it.readText() }
        jsCtx.evaluate(js)

        jsCtx
    } catch (e: Exception) {
        Log.e("JSTranscriber", "Failed to initialize QuickJS context", e)
        null
    }

    fun getTranscription(text: String): String {
        val s = text.escapeJs()
        val call = """
            (function(){
              var t = new CorrentText_Old();
              return t.GetTranscription("$s");
            })();
        """.trimIndent()
        return jsCtx?.evaluate(call)?.toString() ?: text
    }

    fun getTranscription_Alternative(text: String): String {
        val s = text.escapeJs()
        val call = """
            (function(){
              var t = new CorrentText_Old();
              return t.GetTranscription_Alternative("$s");
            })();
        """.trimIndent()
        return jsCtx?.evaluate(call)?.toString() ?: text
    }

    fun close() {
        jsCtx?.destroy()
    }

    private fun String.escapeJs() = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
}