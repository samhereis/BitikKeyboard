package com.shoktuk.shoktukkeyboard.project.systems

import android.content.Context
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.Context as RhinoContext

class JSTranscriber(private val context: Context) {

    private lateinit var rhino: RhinoContext
    private lateinit var scope: ScriptableObject

    init {
        // Initialize Rhino JS engine
        rhino = RhinoContext.enter()
        rhino.optimizationLevel = -1 // Disable optimizations for Android
        scope = rhino.initStandardObjects()

        // Load the ES5-compatible JS file (transcriber.js) from the assets folder
        val jsCode = context.assets.open("transcriber.js").bufferedReader().use { it.readText() }
        // Evaluate the JS file in the Rhino context
        rhino.evaluateString(scope, jsCode, "transcriber.js", 1, null)
    }

    fun getTranscription(inputText: String): String {
        // Instantiate the JS class and call the GetTranscription method
        val result = rhino.evaluateString(
            scope, "new CorrentText().GetTranscription('$inputText')", "GetTranscription", 1, null
        )
        return result.toString()
    }

    fun cleanup() {
        RhinoContext.exit()
    }
}
