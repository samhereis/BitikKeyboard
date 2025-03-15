package com.shoktuk.shoktukkeyboard.project.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screens(val id: String) {
    HOW_TO_ENABLE("HowToEnable_Screen"), TEST_KEYBOARD("TestKeyboard_Screen"), BASIC_INFO("BasicInfo_Screen");

    val title: String
        get() = when (this) {
            HOW_TO_ENABLE -> "How to enable"
            TEST_KEYBOARD -> "Test keyboard"
            BASIC_INFO -> "Basic info"
            else -> "Shoktuk Keyboard"
        }

    val systemImageName: ImageVector
        get() = when (this) {
            HOW_TO_ENABLE -> Icons.Filled.Settings
            TEST_KEYBOARD -> Icons.Filled.CheckCircle
            BASIC_INFO -> Icons.Filled.Info
            else -> Icons.Default.KeyboardArrowUp
        }
}

data class SideMenuItem(
    val title: String, var icon: ImageVector
)