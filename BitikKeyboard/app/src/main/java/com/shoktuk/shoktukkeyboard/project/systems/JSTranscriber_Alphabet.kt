import android.content.Context
import app.cash.quickjs.QuickJs
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant

class JSTranscriber_Alphabet(context: Context) {
    private val quickJs: QuickJs = QuickJs.create()

    init {
        var jsFile = if (context.keyboardVariant == BitikVariant.SAMAGAN) "transcriber_alphabet_modern.js" else "transcriber_alphabet.js"

        val js = context.assets.open(jsFile).bufferedReader(Charsets.UTF_8).use { it.readText() }
        quickJs.evaluate(js, jsFile)
    }

    fun getTranscription(text: String): String = call(text)

    private fun call(input: String): String {
        val escaped = input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
        val jsCall = """
            (function(){
              var t = new Transcrptiber_Old();
              return t.GetTranscription("$escaped");
            })();
        """.trimIndent()
        return quickJs.evaluate(jsCall, "TranscribeCall.js")?.toString() ?: ""
    }

    fun close() {
        quickJs.close()
    }
}
