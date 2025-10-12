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
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.project.data.ColoringStatus
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.coloring
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun KeyButton(
    modifier: Modifier = Modifier,
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
    val bgIndex =
        if (KeyboardViewControllerBase.context.coloring != ColoringStatus.On) 1 else (backgroundColorIndex ?: 1)
    val showPreview = remember { mutableStateOf(false) }
    val isHolding = remember { mutableStateOf(false) }
    val keyHeight = with(LocalDensity.current) { ((KeyboardStyle.rowHeight().value * scale).dp) }

    val colors = KeyboardStyle.colors()
    val baseBg = colors[bgIndex.coerceIn(0, colors.lastIndex)]
    val effTextColor = textColor ?: (if (isShiftEnabled.value && !isSystem) colors[3.coerceIn(
        0, colors.lastIndex
    )] else colors[2.coerceIn(0, colors.lastIndex)])

    val tamga = remember(
        key, title, isShiftEnabled.value
    ) { key?.let { if (isShiftEnabled.value) it.uppercase else it.lowercase } ?: (title ?: "") }
    val hintPrimary = remember(
        key, isShiftEnabled.value
    ) { key?.let { if (isShiftEnabled.value) it.upperCaseRomanization else it.lowerCaseRomanization } }
    val hintSecondary = remember(
        key, isShiftEnabled.value
    ) { key?.let { if (isShiftEnabled.value) it.upperCaseRomanization2 else it.lowerCaseRomanization2 } }
    val tamgaHold = remember(
        key, isShiftEnabled.value
    ) { key?.let { if (isShiftEnabled.value) it.upperCaseHold else it.lowerCaseHold } }
    val hintHold = remember(
        key, isShiftEnabled.value
    ) { key?.let { if (isShiftEnabled.value) it.upperCaseHoldHint else it.lowerCaseHoldHint } ?: "" }
    val showCorner = remember(
        key, isShiftEnabled.value
    ) { KeyboardViewControllerBase.context.coloring != ColoringStatus.Off && key != null && (if (isShiftEnabled.value) key.upperCaseHold != null else key.lowerCaseHold != null) }
    val showTranscription =
        KeyboardViewControllerBase.keyboardMode == KeyboardState.Symbols || KeyboardViewControllerBase.context.letterTranscription == LetterTranscription.On

    val previewBg = remember(isHolding, key, isShiftEnabled.value, baseBg, colors) {
        if (!isHolding.value || key == null) baseBg
        else {
            val idx =
                if (isShiftEnabled.value) key.backgroundColorIndexUppercaseHold else key.backgroundColorIndexLowercaseHold
            idx?.let { colors[it.coerceIn(0, colors.lastIndex)] } ?: baseBg
        }
    }

    LaunchedEffect(isHolding, showPreview) {
        while (isHolding.value && showPreview.value) {
            whilePressing()
            delay(100)
        }
    }

    val density = LocalDensity.current
    var anchorBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Box( // outer host
        modifier = modifier
            .offset(x = additionalOffsetX)
            .padding(
                start = sidePaddingLeft,
                end = sidePaddingRight,
                top = KeyboardStyle.keyTopPadding,
                bottom = KeyboardStyle.keyTopPadding
            )
            .then(if (width != null) Modifier.width(width * scale) else Modifier)
            .height(keyHeight)
            .onGloballyPositioned { coords ->
                anchorBounds = coords.boundsInWindow()
            }) {
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .matchParentSize()
                .alpha(if (showPreview.value) 0.25f else 1f)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        showPreview.value = true
                        val released = try {
                            tryAwaitRelease()
                        } catch (_: Throwable) {
                            false
                        }
                        if (released) onKeyPress()
                        isHolding.value = false
                        showPreview.value = false
                    }, onLongPress = {
                        isHolding.value = true
                        showPreview.value = true
                        onLongPress()
                    })
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { showPreview.value = true },
                        onDragEnd = { isHolding.value = false; showPreview.value = false },
                        onDragCancel = { isHolding.value = false; showPreview.value = false },
                        onDrag = { _, _ -> })
                }) {
            KeyFaceContent(
                tamga = tamga,
                icon = icon,
                textColor = effTextColor,
                bg = baseBg,
                corner = KeyboardStyle.buttonCornerRadius,
                showTranscription = showTranscription,
                hintTop = hintSecondary,
                hintBottom = hintPrimary,
                showCornerStripe = showCorner,
                isSystem = isSystem,
                scale = scale
            )
        }
    }

    if (showPreview.value && previewOnTap) {
        val anchor = anchorBounds
        val previewText = if (isHolding.value) (tamgaHold ?: tamga) else tamga
        val previewHintBottom =
            if (isHolding.value) (if (hintHold.isEmpty()) hintPrimary else hintHold) else hintPrimary
        val gapPx = with(density) { 6.dp.toPx().toInt() }
        val keyHpx = with(density) { keyHeight.toPx().toInt() }

        if (anchor != null) {
            val keyWpx = if (width != null) {
                with(density) { (width * scale).toPx().toInt() }
            } else {
                anchor.width.roundToInt()
            }

            androidx.compose.ui.window.Popup(
                popupPositionProvider = object : androidx.compose.ui.window.PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: androidx.compose.ui.unit.IntRect,
                        windowSize: androidx.compose.ui.unit.IntSize,
                        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                        popupContentSize: androidx.compose.ui.unit.IntSize
                    ) = androidx.compose.ui.unit.IntOffset(
                        x = anchor.left.roundToInt(), y = (anchor.top - gapPx - keyHpx).roundToInt()
                    )
                }, properties = androidx.compose.ui.window.PopupProperties(
                    focusable = false, clippingEnabled = false
                ), onDismissRequest = { showPreview.value = false }) {
                Box(
                    modifier = Modifier
                        .width(with(density) { keyWpx.toDp() })
                        .height(keyHeight)
                ) {
                    KeyFaceContent(
                        tamga = previewText,
                        icon = icon,
                        textColor = effTextColor,
                        bg = previewBg,
                        KeyboardStyle.buttonCornerRadius,
                        showTranscription = showTranscription,
                        hintTop = hintSecondary,
                        hintBottom = previewHintBottom,
                        showCornerStripe = showCorner,
                        isSystem = isSystem,
                        scale = scale
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .then(if (width != null) Modifier.width(width * scale) else Modifier.fillMaxWidth())
                    .height(keyHeight)
                    .offset(y = (-6).dp)
            ) {
                KeyFaceContent(
                    tamga = previewText,
                    icon = icon,
                    textColor = effTextColor,
                    bg = previewBg,
                    KeyboardStyle.buttonCornerRadius,
                    showTranscription = showTranscription,
                    hintTop = hintSecondary,
                    hintBottom = previewHintBottom,
                    showCornerStripe = showCorner,
                    isSystem = isSystem,
                    scale = scale
                )
            }
        }
    }
}

@Composable
private fun KeyFaceContent(
    tamga: String,
    icon: @Composable (() -> Unit)?,
    textColor: Color?,
    bg: Color,
    corner: Dp,
    showTranscription: Boolean,
    hintTop: String?,
    hintBottom: String?,
    showCornerStripe: Boolean,
    isSystem: Boolean,
    scale: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(corner))
            .background(bg)
    ) {
        if (icon != null) {
            Box(
                Modifier
                    .size((20 * scale).dp)
                    .align(Alignment.Center)
            ) { icon() }
        } else {
            Text(
                text = tamga,
                color = textColor ?: Color.Unspecified,
                style = if (isSystem) KeyboardStyle.buttonFont else KeyboardViewControllerBase.fontScale,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (showTranscription && !hintTop.isNullOrEmpty()) {
            Text(
                hintTop,
                color = textColor ?: Color.Unspecified,
                fontSize = (10 * scale).sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = (2 * scale).dp)
            )
        }
        if (showTranscription && !hintBottom.isNullOrEmpty()) {
            Text(
                hintBottom,
                color = textColor ?: Color.Unspecified,
                fontSize = (10 * scale).sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (2 * scale).dp)
            )
        }

        if (showCornerStripe) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        color = textColor ?: Color.Unspecified,
                    )
            )
        }
    }
}