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

        // Load JS file
        val jsCode = context.assets.open("transcriber.js").bufferedReader().use { it.readText() }
        rhino.evaluateString(scope, jsCode, "transcriber.js", 1, null) // ERROR HERE
    }

    fun getTranscription(inputText: String): String {
        // Instantiate JS class and call method
        val result = rhino.evaluateString(
            scope, "new CorrentText().getTranscription('$inputText')", "getTranscription", 1, null
        )
        return result.toString()
    }

    fun cleanup() {
        RhinoContext.exit()
    }
}