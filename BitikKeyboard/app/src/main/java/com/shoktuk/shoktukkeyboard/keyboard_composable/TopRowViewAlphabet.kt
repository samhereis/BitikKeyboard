package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TopRowView_Alphabet(
    onAlphabetChange: () -> Unit = {}
) {
    val cornerRadius = 12.dp
    val innerGlowWidth = 15f
    val innerGlowBlur = 15.dp
    val innerGlowOpacity = 0.9f
    val outerGlowWidth = 3f
    val outerGlowBlur = 3.dp

    val colors = listOf(
        Color.Red, Color(0xFFFF9800), Color.Yellow, Color(0xFF4CAF50),
        Color.Cyan, Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFFF4081), Color.Red
    )

    // Use companion state for reactive isBitikMode
    val isBitikMode = KeyboardViewControllerBase.isBitikModeState
    val transcription = KeyboardViewControllerBase.alphabetTranscriptionState.value
    val alphabetLabel = KeyboardViewControllerBase.alphabetLabelState.value
    val ctx = LocalContext.current

    val infinite = rememberInfiniteTransition(label = "rot")
    val rotation by if (isBitikMode.value) {
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
            label = "angle"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val brush = Brush.sweepGradient(colors)
    val density = LocalDensity.current
    val keyRowHeight: Dp = with(density) { (KeyboardStyle.rowHeight().value.dp * 1.0f) }

    Surface(color = Color.Transparent) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            // Language/alphabet switcher
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        color = KeyboardStyle.getColor(6),
                        shape = MaterialTheme.shapes.small
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { onAlphabetChange() }
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = alphabetLabel,
                    color = KeyboardStyle.getColor(2),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Center glow box with transcription
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyRowHeight)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(KeyboardStyle.getColor(0).copy(alpha = 0.01f))
                )

                val pressed = remember { mutableStateOf(false) }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 3.dp, horizontal = 25.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KeyboardStyle.getColor(1).copy(alpha = 0.5f))
                        .drawThinStroke(Color.Gray.copy(alpha = 0.4f))
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                pressed.value = true
                                tryAwaitRelease()
                                pressed.value = false
                                // Replace last word with Bitik transcription
                                if (isBitikMode.value && transcription.isNotEmpty()) {
                                    KeyboardViewControllerBase.context.replaceWithBitikTranscription()
                                }
                            })
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { pressed.value = true },
                                onDragEnd = { pressed.value = false },
                                onDragCancel = { pressed.value = false },
                                onDrag = { _, _ -> }
                            )
                        }
                        .graphicsLayer {
                            scaleX = if (pressed.value) 0.9f else 1f
                            scaleY = if (pressed.value) 0.9f else 1f
                        }
                ) {
                    if (isBitikMode.value && transcription.isNotEmpty()) {
                        Text(
                            text = transcription,
                            color = KeyboardStyle.getColor(2),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    } else if (!isBitikMode.value) {
                        Text(
                            text = "👆",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                            color = KeyboardStyle.getColor(2).copy(alpha = 0.4f)
                        )
                    }
                }

                GlowOverlay(
                    rotation = rotation,
                    active = isBitikMode.value,
                    corner = cornerRadius,
                    brush = brush,
                    innerWidth = innerGlowWidth,
                    innerBlur = innerGlowBlur,
                    innerOpacity = innerGlowOpacity,
                    outerWidth = outerGlowWidth,
                    outerBlur = outerGlowBlur
                )

                // Full-area tap to toggle isBitikMode
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(Color.Transparent)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                KeyboardViewControllerBase.isBitikMode = !KeyboardViewControllerBase.isBitikMode
                            }
                        }
                )
            }

            // IME picker
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        color = KeyboardStyle.getColor(6),
                        shape = MaterialTheme.shapes.small
                    )
                    .pointerInput(Unit) {
                        detectTapGestures {
                            val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showInputMethodPicker()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⌨",
                    color = KeyboardStyle.getColor(2),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun GlowOverlay(
    rotation: Float,
    active: Boolean,
    corner: Dp,
    brush: Brush,
    innerWidth: Float,
    innerBlur: Dp,
    innerOpacity: Float,
    outerWidth: Float,
    outerBlur: Dp
) {
    val alphaInner = if (active) innerOpacity else 0.01f
    val alphaOuter = if (active) 1f else 0.01f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationZ = rotation }
    ) {
        if (outerWidth > 0f || outerBlur > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(corner))
                    .borderBlur(brush = brush, width = outerWidth, blur = outerBlur, alpha = alphaOuter)
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(corner))
                .borderBlur(brush = brush, width = innerWidth, blur = innerBlur, alpha = alphaInner)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(corner))
                .borderBlur(brush = brush, width = 2f, blur = 0.dp, alpha = if (active) 1f else 0.01f)
        )
    }
}

private fun Modifier.borderBlur(brush: Brush, width: Float, blur: Dp, alpha: Float) = this.then(
    Modifier
        .drawWithStroke(brush, width, alpha)
        .blur(blur, BlurredEdgeTreatment.Unbounded)
)

private fun Modifier.drawWithStroke(brush: Brush, width: Float, alpha: Float) = this.composed {
    this.then(
        Modifier.drawBehind {
            drawRoundRect(
                brush = brush,
                style = Stroke(width),
                alpha = alpha,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension / 12f)
            )
        }
    )
}

private fun Modifier.drawThinStroke(color: Color) = this.then(
    Modifier.drawBehind {
        drawRoundRect(
            color = color,
            style = Stroke(1f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension / 12f)
        )
    }
)

@Preview
@Composable
fun TopRowViewAlphabetPreview() {
    MaterialTheme {
        TopRowView_Alphabet()
    }
}
