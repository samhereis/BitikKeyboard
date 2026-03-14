package com.shoktuk.shoktukkeyboard.keyboard

import java.util.UUID

data class KeyboardKey(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var lowercase: String,
    var lowerCaseRomanization: String? = null,
    var lowerCaseRomanization2: String? = null,
    var lowerCaseHold: String? = null,
    var lowerCaseHoldHint: String? = null,
    var backgroundColorIndexLowercase: Int? = 1,
    var backgroundColorIndexLowercaseHold: Int? = 1,
    var uppercase: String,
    var upperCaseRomanization: String? = null,
    var upperCaseRomanization2: String? = null,
    var upperCaseHold: String? = null,
    var upperCaseHoldHint: String? = null,
    var backgroundColorIndexUppercase: Int? = 1,
    var backgroundColorIndexUppercaseHold: Int? = 1
) {
    init {
        if (backgroundColorIndexLowercase != null && backgroundColorIndexLowercaseHold == null) {
            backgroundColorIndexLowercaseHold = backgroundColorIndexLowercase
        }
        if (backgroundColorIndexUppercase != null && backgroundColorIndexUppercaseHold == null) {
            backgroundColorIndexUppercaseHold = backgroundColorIndexUppercase
        }
    }
}
