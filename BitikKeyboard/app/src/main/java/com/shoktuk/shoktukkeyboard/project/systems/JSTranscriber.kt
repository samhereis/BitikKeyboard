import android.content.Context
import android.util.Log
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.QuickJSContext
import org.json.JSONObject

class JSTranscriber(context: Context) {
    companion object {
        private const val TAG = "JSTranscriber"
        private const val JS_ERROR_PREFIX = "__js_error__:"
    }

    private val jsCtx: QuickJSContext? = try {
        QuickJSLoader.init()
        val ctx = QuickJSContext.create()
        val jsFile = if (context.keyboardVariant == BitikVariant.CLASSIC) "transcriber_old.js" else "transcriber_modern.js"
        val js = context.assets.open(jsFile).bufferedReader(Charsets.UTF_8).use { it.readText() }
        ctx.evaluate(js)
        ctx
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize QuickJS context", e)
        null
    }

    fun getTranscription(text: String): String {
        return safeCall("GetTranscription", text)
    }

    fun getTranscription_Alternative(text: String): String {
        return safeCall("GetTranscription_Alternative", text)
    }

    fun close() {
        try {
            jsCtx?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error while destroying JS context", e)
        }
    }

    private fun safeCall(functionName: String, input: String): String {
        val quoted = try {
            JSONObject.quote(input)
        } catch (e: Exception) {
            Log.w(TAG, "JSONObject.quote failed, falling back to manual escaping", e)
            "\"" + input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\""
        }

        val code = """
            (function(){
              try {
                var t = new CorrentText_Old();
                var result = t.$functionName($quoted);
                if (result === undefined || result === null) return "";
                return result;
              } catch (e) {
                return "$JS_ERROR_PREFIX" + (e && e.message ? e.message : String(e));
              }
            })();
        """.trimIndent()

        return try {
            val res = jsCtx?.evaluate(code)?.toString()
            if (res == null) {
                Log.w(TAG, "JS evaluation returned null; returning original input")
                input
            } else if (res.startsWith(JS_ERROR_PREFIX)) {
                val msg = res.removePrefix(JS_ERROR_PREFIX)
                Log.e(TAG, "JS runtime error calling $functionName: $msg")
                input
            } else {
                res
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while evaluating JS for $functionName", e)
            input
        }
    }
}