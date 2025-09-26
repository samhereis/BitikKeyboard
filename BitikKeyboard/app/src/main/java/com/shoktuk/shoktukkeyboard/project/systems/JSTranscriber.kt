import android.content.Context
import app.cash.quickjs.QuickJs
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant

class JSTranscriber(context: Context) {
    private val quickJs: QuickJs = QuickJs.create()

    init {
        var jsFile = if(context.keyboardVariant == BitikVariant.CLASSIC) "transcriber_old.js" else "transcriber_modern.js"

        val js = context.assets.open(jsFile).bufferedReader(Charsets.UTF_8).use { it.readText() }
        quickJs.evaluate(js, jsFile)
    }

    fun getTranscription(text: String): String {
        val escaped = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
        val jsCall = """
            (function(){
              var t = new CorrentText_Old();
              return t.GetTranscription("$escaped");
            })();
        """.trimIndent()
        return quickJs.evaluate(jsCall, "TranscribeCall.js")?.toString() ?: ""
    }

    fun getTranscription_Alternative(text: String): String {
        val escaped = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
        val jsCall = """
            (function(){
              var t = new CorrentText_Old();
              return t.GetTranscription_Alternative("$escaped");
            })();
        """.trimIndent()
        return quickJs.evaluate(jsCall, "TranscribeCall.js")?.toString() ?: ""
    }

    fun close() {
        quickJs.close()
    }
}
