package com.shoktuk.shoktukkeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.project.data.ColoringStatus
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.coloring
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import kotlinx.coroutines.delay

@Composable
fun KeyButton(
    key: KeyboardKey? = null,
    title: String? = null,
    icon: (@Composable () -> Unit)? = null,
    isSystem: Boolean,
    width: Dp? = null,
    backgroundColorIndex: Int? = 0,
    textColor: Color? = null,
    scale: Float = 1f,
    previewOnTap: Boolean = false,
    isShiftEnabled: MutableState<Boolean> = mutableStateOf(true),
    onKeyPress: () -> Unit = {},
    onLongPress: () -> Unit = {},
    whilePressing: () -> Unit = {},
    sidePaddingLeft: Dp = KeyboardStyle.keySidePadding,
    sidePaddingRight: Dp = KeyboardStyle.keySidePadding,
    additionalOffsetX: Dp = 0.dp
) {
    val bgIndex = if (KeyboardViewControllerBase.context.coloring != ColoringStatus.On) 1 else (backgroundColorIndex ?: 1)
    var showPreview by remember { mutableStateOf(false) }
    var isHolding by remember { mutableStateOf(false) }
    val corner = KeyboardStyle.buttonCornerRadius * scale
    val effWidth = width?.let { it * scale }
    val keyHeight = with(LocalDensity.current) { ((KeyboardStyle.rowHeight().value * scale).dp) }

    val colors = KeyboardStyle.colors()
    val baseBg = colors[bgIndex.coerceIn(0, colors.lastIndex)]
    val effTextColor = textColor ?: (if (isShiftEnabled.value && !isSystem) colors[3.coerceIn(0, colors.lastIndex)] else colors[2.coerceIn(0, colors.lastIndex)])

    val tamga = remember(key, title, isShiftEnabled.value) {
        key?.let { if (isShiftEnabled.value) it.uppercase else it.lowercase } ?: (title ?: "")
    }
    val hintPrimary = remember(key, isShiftEnabled.value) {
        key?.let { if (isShiftEnabled.value) it.upperCaseRomanization else it.lowerCaseRomanization }
    }
    val hintSecondary = remember(key, isShiftEnabled.value) {
        key?.let { if (isShiftEnabled.value) it.upperCaseRomanization2 else it.lowerCaseRomanization2 }
    }
    val tamgaHold = remember(key, isShiftEnabled.value) {
        key?.let { if (isShiftEnabled.value) it.upperCaseHold else it.lowerCaseHold }
    }
    val hintHold = remember(key, isShiftEnabled.value) {
        key?.let { if (isShiftEnabled.value) it.upperCaseHoldHint else it.lowerCaseHoldHint } ?: ""
    }
    val showCorner = remember(key, isShiftEnabled.value) {
        KeyboardViewControllerBase.context.coloring != ColoringStatus.Off && key != null && (if (isShiftEnabled.value) key.upperCaseHold != null else key.lowerCaseHold != null)
    }

    val showTranscription = KeyboardViewControllerBase.keyboardMode == KeyboardState.Symbols || KeyboardViewControllerBase.context.letterTranscription == LetterTranscription.On

    val previewBg = remember(isHolding, key, isShiftEnabled.value, baseBg, colors) {
        if (!isHolding || key == null) baseBg
        else {
            val idx = if (isShiftEnabled.value) key.backgroundColorIndexUppercaseHold else key.backgroundColorIndexLowercaseHold
            idx?.let { colors[it.coerceIn(0, colors.lastIndex)] } ?: baseBg
        }
    }

    LaunchedEffect(isHolding, showPreview) {
        while (isHolding && showPreview) {
            whilePressing()
            delay(100)
        }
    }

    Surface(
        color = Color.Transparent, modifier = Modifier
            .offset(x = additionalOffsetX)
            .padding(
                start = sidePaddingLeft, end = sidePaddingRight, top = KeyboardStyle.keyTopPadding, bottom = KeyboardStyle.keyTopPadding
            )
            .then(if (effWidth != null) Modifier.width(effWidth) else Modifier)
            .height(keyHeight)
            .clip(RoundedCornerShape(corner))
            .background(Color.Black.copy(alpha = 0.001f))
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    isHolding = true
                    showPreview = true
                    onLongPress()
                }, onPress = {
                    showPreview = true
                    val released = try {
                        tryAwaitRelease()
                    } catch (_: Throwable) {
                        false
                    }
                    if (released) {
                        if (!isHolding) {
                            onKeyPress()
                        } else {
                            onKeyPress()
                        }
                    }
                    isHolding = false
                    showPreview = false
                })
            }
            .pointerInput(Unit) {
                detectDragGestures(onDragStart = {
                    showPreview = true
                }, onDragEnd = {
                    isHolding = false
                    showPreview = false
                }, onDragCancel = {
                    isHolding = false
                    showPreview = false
                }, onDrag = { _, _ -> })
            }
            .alpha(if (showPreview) 0.25f else 1f)) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(baseBg, RoundedCornerShape(corner)), contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Box(Modifier.size((20 * scale).dp)) { icon() }
                } else {
                    Text(
                        text = tamga,
                        color = effTextColor,
                        style = if (isSystem) KeyboardStyle.buttonFont else KeyboardViewControllerBase.fontScale,
                    )
                }
            }
            if (showTranscription && hintSecondary != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = (2 * scale).dp), contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = hintSecondary, color = effTextColor, style = TextStyle(fontSize = (10 * scale).sp)
                    )
                }
            }
            if (showTranscription && hintPrimary != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = (2 * scale).dp), contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = hintPrimary, color = effTextColor, style = TextStyle(fontSize = (10 * scale).sp)
                    )
                }
            }
            if (showCorner) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(effTextColor)
                        .align(Alignment.TopCenter)
                )
            }
        }
    }

    if (showPreview && previewOnTap) {
        Box(
            modifier = Modifier.padding(start = sidePaddingLeft, end = sidePaddingRight)
        ) {
            Box(
                modifier = Modifier
                    .then(if (effWidth != null) Modifier.width(effWidth) else Modifier)
                    .height(keyHeight)
                    .offset(y = (-35).dp)
                    .clip(RoundedCornerShape(corner))
                    .background(previewBg),
                contentAlignment = Alignment.CenterStart
            ) {
                val showText = if (isHolding) tamgaHold ?: tamga else tamga
                Text(
                    text = showText, color = effTextColor, style = KeyboardStyle.buttonFont, modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 2.dp)
                )
                val hintToShow = if (isHolding) if (hintHold.isEmpty()) hintPrimary else hintHold else hintPrimary
                if (!hintToShow.isNullOrEmpty()) {
                    Text(
                        text = hintToShow,
                        color = effTextColor,
                        style = TextStyle(fontSize = (10 * scale).sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (4 * scale).dp, bottom = (2 * scale).dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun KeyButtonPreview() {
    KeyButton(
        icon = { Text("⇧", fontWeight = FontWeight.Bold) }, isSystem = true, width = 60.dp, backgroundColorIndex = 1
    )
}
