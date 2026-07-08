package com.shoktuk.shoktukkeyboard.keyboard;

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.sounds
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.vibrations
import com.shoktuk.shoktukkeyboard.project.data.Sounds
import com.shoktuk.shoktukkeyboard.project.data.Vibrations

object ClickSounds {
    fun click(view: View) {
        if (MyKeyboardService.context.sounds == Sounds.On) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    fun navLeft(view: View) {
        view.playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT)
    }

    fun navRight(view: View) {
        view.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT)
    }

    fun navUp(view: View) {
        view.playSoundEffect(SoundEffectConstants.NAVIGATION_UP)
    }

    fun navDown(view: View) {
        view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)
    }
}

/**
 * Compose helper for the new keyboard: returns a callback that plays the click
 * sound (if the Sounds setting is On) and a haptic tap (if the Vibrations setting
 * is On). Call it from every button's tap handler.
 */
@Composable
fun rememberKeyFeedback(): () -> Unit {
    val view = LocalView.current
    return remember(view) {
        {
            val ctx = KeyboardViewControllerBase.context
            if (ctx.sounds == Sounds.On) {
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
            if (ctx.vibrations == Vibrations.On) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }
    }
}