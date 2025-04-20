package com.shoktuk.shoktukkeyboard.project.systems

import android.content.Context
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
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

        var fileName = "transcriber.js"
        if (getIsClassic()) {
            fileName = "transcriber_old.js"
        }

        val jsCode = context.assets.open(fileName).bufferedReader().use { it.readText() }
        rhino.evaluateString(scope, jsCode, fileName, 1, null)
    }

    fun getIsClassic(): Boolean {
        return SettingsManager.getKeyboardVariant(context) == KeyboardVariant.CLASSIC;
    }

    fun getTranscription(inputText: String): String {
        var methodCall = "new CorrentText().GetTranscription('$inputText')";
        if (getIsClassic()) {
            methodCall = "new CorrentText_Old().GetTranscription('$inputText')";
        }

        val result = rhino.evaluateString(
            scope, methodCall, "GetTranscription", 1, null
        )
        return result.toString()
    }

    fun getTranscription_Alternative(inputText: String): String {
        var methodCall = "new CorrentText().GetTranscription_Alternative('$inputText')";
        if (getIsClassic()) {
            methodCall = "new CorrentText_Old().GetTranscription_Alternative('$inputText')";
        }

        val result = rhino.evaluateString(
            scope, methodCall, "GetTranscription_Alternative", 1, null
        )
        return result.toString()
    }

    fun cleanup() {
        RhinoContext.exit()
    }
}
