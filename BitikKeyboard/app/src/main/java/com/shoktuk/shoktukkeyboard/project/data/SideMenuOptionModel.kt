package com.shoktuk.shoktukkeyboard.project.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainScreens(val id: String) {
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

enum class BasicInfoScreens(val id: String) {
    ORIGINAL_TAMGAS("OriginalTamgasView"), MODERNIZED_TAMGAS("ModernizedTamgasView"), RULES_OF_WRITING("HowToEnable_Screen");

    val title: String
        get() = when (this) {
            ORIGINAL_TAMGAS -> "Original Tamgas"
            MODERNIZED_TAMGAS -> "Modernized Tamgas"
            RULES_OF_WRITING -> "Rules of writing"
            else -> "Shoktuk Keyboard"
        }

    val systemImageName: ImageVector
        get() = when (this) {
            ORIGINAL_TAMGAS -> Icons.Filled.Settings
            MODERNIZED_TAMGAS -> Icons.Filled.CheckCircle
            RULES_OF_WRITING -> Icons.Filled.Info
            else -> Icons.Default.KeyboardArrowUp
        }
}


data class SideMenuItem(
    val title: String, var icon: ImageVector
)