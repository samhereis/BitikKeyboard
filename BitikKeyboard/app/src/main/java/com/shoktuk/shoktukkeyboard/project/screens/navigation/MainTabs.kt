package com.shoktuk.shoktukkeyboard.project.screens.navigation

enum class MainTabs(val route: String) {
    HOW_TO_ENABLE("how_to_enable"),
    TEST_KEYBOARD("test_keyboard"),
    BASIC_INFO("basic_info"),
    SETTINGS("settings");

    companion object {
        fun fromRoute(route: String?): MainTabs {
            return when(route) {
                HOW_TO_ENABLE.route -> HOW_TO_ENABLE
                TEST_KEYBOARD.route -> TEST_KEYBOARD
                BASIC_INFO.route -> BASIC_INFO
                SETTINGS.route -> SETTINGS
                else -> HOW_TO_ENABLE
            }
        }
    }
}