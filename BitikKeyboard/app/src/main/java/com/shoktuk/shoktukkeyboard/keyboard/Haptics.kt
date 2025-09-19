import android.view.HapticFeedbackConstants
import android.view.View
import com.shoktuk.shoktukkeyboard.keyboard.ClickSounds
import com.shoktuk.shoktukkeyboard.keyboard.MyKeyboardService
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.vibrations
import com.shoktuk.shoktukkeyboard.project.data.Vibrations

object Haptics {
    fun perform(view: View, type: Int, force: Boolean = false) {
        ClickSounds.click(view)
        if (MyKeyboardService.context.vibrations == Vibrations.On) {
            view.performHapticFeedback(type)
        }
    }

    fun keyTap(view: View, force: Boolean = false) = perform(view, HapticFeedbackConstants.KEYBOARD_TAP, force)

    fun longPress(view: View, force: Boolean = false) = perform(view, HapticFeedbackConstants.LONG_PRESS, force)

    fun contextClick(view: View, force: Boolean = false) = perform(view, HapticFeedbackConstants.CONTEXT_CLICK, force)
}