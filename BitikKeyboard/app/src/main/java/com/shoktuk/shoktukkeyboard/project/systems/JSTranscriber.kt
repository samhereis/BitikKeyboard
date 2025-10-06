import android.content.Context
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant

class JSTranscriber(context: Context) {
    private val v8: V8 = V8.createV8Runtime()
    private val ctorName = "CorrentText_Old"

    init {
        val jsFile = if (context.keyboardVariant == BitikVariant.CLASSIC) "transcriber_old.js"
        else "transcriber_modern.js"

        val js = context.assets.open(jsFile).bufferedReader(Charsets.UTF_8).use { it.readText() }
        v8.executeVoidScript(js, jsFile, 0)
    }

    fun getTranscription(text: String): String = callInstanceMethod("GetTranscription", text)

    fun getTranscription_Alternative(text: String): String = callInstanceMethod("GetTranscription_Alternative", text)

    private fun callInstanceMethod(methodName: String, arg: String): String {
        val instance: V8Object = v8.executeObjectScript("new $ctorName();")
        val args = V8Array(v8).push(arg)
        return try {
            instance.executeStringFunction(methodName, args)
        } catch (_: Throwable) {
            ""
        } finally {
            try {
                args.release()
            } catch (_: Throwable) {
            }
            try {
                instance.release()
            } catch (_: Throwable) {
            }
        }
    }

    fun close() {
        try {
            v8.release()
        } catch (_: Throwable) {
        }
    }
}