package com.shoktuk.shoktukkeyboard.project.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screens(val id: String) {
    HOW_TO_ENABLE("HowToEnable_Screen"), TEST_KEYBAORD("TestKeyboard_Screen"), BASIC_INFO("BasicInfo_Screen");

    val title: String
        get() = when (this) {
            HOW_TO_ENABLE -> "How to enable"
            TEST_KEYBAORD -> "Test keyboard"
            BASIC_INFO -> "Basic info"
            else -> "Shoktuk Keyboard"
        }

    val systemImageName: ImageVector
        get() = when (this) {
            HOW_TO_ENABLE -> Icons.Filled.MailOutline
            TEST_KEYBAORD -> Icons.Filled.MailOutline
            BASIC_INFO -> Icons.Filled.MailOutline
            else -> Icons.Default.KeyboardArrowUp
        }
}

data class SideMenuItem(
    val title: String, var icon: ImageVector
)