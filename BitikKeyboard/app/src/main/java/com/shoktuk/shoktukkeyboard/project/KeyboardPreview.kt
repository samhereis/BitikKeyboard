package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView

interface KeyboardServiceProvider {
    fun createKeyboardView(context: Context): View
}

class RealKeyboardServiceProvider(
    private val service: MyKeyboardService
) : KeyboardServiceProvider {
    override fun createKeyboardView(context: Context): View {
        val layout = KeyboardLayoutLoader.loadKeyboardLayout(context, service.currentMode, service.getLanguage())

        return KeyboardViewBuilder.buildKeyboardView(service = service, layout = layout, isCaps = service.isCaps, true, onCapsChange = { newCaps ->
            service.isCaps = newCaps
            service.reloadKeyboard()
        }, onModeChange = { newMode ->
            service.currentMode = newMode
            service.reloadKeyboard()
        })
    }
}

/**
 * Preview provider: returns a basic TextView so the preview renders without crashing.
 */
class PreviewKeyboardServiceProvider : KeyboardServiceProvider {
    override fun createKeyboardView(context: Context): View {
        return TextView(context).apply {
            text = "🔧 Keyboard Preview"
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
    }
}

/**
 * The composable that embeds your keyboard View.
 * It picks the real or preview provider based on LocalInspectionMode.
 */
@Composable
fun TopRowWrapper() {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    // Remember a single provider instance per composition
    val provider: KeyboardServiceProvider = remember {
        if (isPreview) {
            PreviewKeyboardServiceProvider()
        } else {
            RealKeyboardServiceProvider(MyKeyboardService())
        }
    }

    AndroidView(
        factory = { ctx ->
            provider.createKeyboardView(ctx)
        }, modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun TopRowWrapperPreview() {
    TopRowWrapper()
}
