package com.shoktuk.shoktukkeyboard.keyboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Composable
fun TopRowView_Alphabet(
    onAlphabetChange: () -> Unit = {}
) {
    val isBitikMode = KeyboardViewControllerBase.isAutoWriteBitikMode
    val transcription = KeyboardViewControllerBase.alphabetTranscriptionState.value

    val infinite = rememberInfiniteTransition(label = "glowRot")
    val rotation by if (isBitikMode.value) {
        infinite.animateFloat(
            initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "angle"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val glowBrush = Brush.sweepGradient(
        listOf(
            Color.Red, Color(0xFFFF9800), Color.Yellow, Color(0xFF4CAF50), Color.Cyan, Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFFF4081), Color.Red
        )
    )

    val pillPressed = remember { mutableStateOf(false) }
    val keyFeedback = rememberKeyFeedback()

    TopRowFrame(
        alphabetLabel = KeyboardViewControllerBase.alphabetLabelState.value,
        onAlphabetChange = onAlphabetChange,
        centerModifier = Modifier.pointerInput(Unit) {
            detectTapGestures {
                keyFeedback()
                KeyboardViewControllerBase.isAutoWriteBitikMode.value = !KeyboardViewControllerBase.isAutoWriteBitikMode.value
                KeyboardViewControllerBase.context.refreshTranscription()
            }
        }
    ) {
        if (!isBitikMode.value) {
            Text(
                text = "👆",
                style = MaterialTheme.typography.titleMedium,
                color = KeyboardStyle.getColor(2).copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            )
        }

        if (transcription.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(28.dp)
                    .widthIn(min = 28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(KeyboardStyle.getColor(6))
                    .graphicsLayer {
                        scaleX = if (pillPressed.value) 0.9f else 1f
                        scaleY = if (pillPressed.value) 0.9f else 1f
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            pillPressed.value = true
                            tryAwaitRelease()
                            pillPressed.value = false
                            keyFeedback()
                            KeyboardViewControllerBase.context.replaceWithBitikTranscription()
                        })
                    }
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = transcription, color = KeyboardStyle.getColor(2), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), fontFamily = KeyboardStyle.bitikFontFamily(alwaysBitik = true), maxLines = 1
                )
            }
        }

        GlowOverlay(
            rotation = rotation, active = isBitikMode.value, corner = 12.dp, brush = glowBrush, innerWidth = 15f, innerBlur = 15.dp, innerOpacity = 0.9f, outerWidth = 3f, outerBlur = 3.dp
        )
    }
}

@Composable
private fun GlowOverlay(
    rotation: Float, active: Boolean, corner: Dp, brush: Brush, innerWidth: Float, innerBlur: Dp, innerOpacity: Float, outerWidth: Float, outerBlur: Dp
) {
    val ai = if (active) innerOpacity else 0.01f
    val ao = if (active) 1f else 0.01f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationZ = rotation }) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(corner))
                .borderBlur(brush, outerWidth, outerBlur, ao)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(corner))
                .borderBlur(brush, innerWidth, innerBlur, ai)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(corner))
                .borderBlur(brush, 2f, 0.dp, if (active) 1f else 0.01f)
        )
    }
}

private fun Modifier.borderBlur(brush: Brush, width: Float, blur: Dp, alpha: Float) = this
    .drawWithStroke(brush, width, alpha)
    .blur(blur, BlurredEdgeTreatment.Unbounded)

private fun Modifier.drawWithStroke(brush: Brush, width: Float, alpha: Float) = this.composed {
    drawBehind {
        drawRoundRect(
            brush = brush, style = Stroke(width), alpha = alpha, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension / 12f)
        )
    }
}

@Preview(name = "TopRowView_Alphabet – Light", showBackground = true)
@Composable
private fun TopRowViewAlphabetPreview_Light() {
    ShoktukKeyboardTheme(darkTheme = false) { TopRowView_Alphabet() }
}

@Preview(name = "TopRowView_Alphabet – Dark", showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopRowViewAlphabetPreview_Dark() {
    ShoktukKeyboardTheme(darkTheme = true) { TopRowView_Alphabet() }
}
