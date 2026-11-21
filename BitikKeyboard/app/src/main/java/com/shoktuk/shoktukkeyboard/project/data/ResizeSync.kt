package com.shoktuk.shoktukkeyboard.project.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * The keyboard's resize mode changes the height and bottom offset settings while the settings screens, which show them as sliders,
 * may be on screen in the same process. The resize mode bumps [tick] and the screens read their sliders again.
 */
object ResizeSync {
    var tick by mutableIntStateOf(0)
}
