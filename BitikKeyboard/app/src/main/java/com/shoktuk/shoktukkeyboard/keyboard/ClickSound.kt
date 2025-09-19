package com.shoktuk.shoktukkeyboard.keyboard;

import android.view.SoundEffectConstants
import android.view.View
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.sounds
import com.shoktuk.shoktukkeyboard.project.data.Sounds


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