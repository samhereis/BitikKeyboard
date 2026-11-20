package com.shoktuk.shoktukkeyboard.store

import Language
import LocalizationManager
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.shoktuk.shoktukkeyboard.R

/*
 * The Play Store banners, the Android counterpart of the iOS `StoreBannerKit`: the same header, captions, card look and
 * texts. Everything on a banner is live: the keyboard cards are the real keyboard (StoreKeyboardHost.kt) and the screen cards are
 * the app's real screens, so a banner can never show something the app does not do.
 */

object StoreStyle {
    val background = Color(0xFFE4E7F8)
    val blue = Color(0xFF435DA6)
    val keyboardBackground = Color(0xFFEDEBF3)
    val send = Color(0xFF6C4DF5)
}

/** The language of the banner being drawn (`en`, `ky_kiril`, `tr`): picks the folder of the keyboard pictures and the locale for upper-casing. */
val LocalStoreLanguage = compositionLocalOf { "en" }

private val storeCardCorner = 26.dp

private val rounded = FontFamily.SansSerif

fun storeLocale(language: String): Locale = Locale.forLanguageTag(if (language.startsWith("ky")) "ky" else language)

class StoreText(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val captions: List<String> = emptyList(),
)

@Composable
fun StoreBannerFrame(text: StoreText, content: @Composable ColumnScope.() -> Unit) {
    val locale = storeLocale(LocalStoreLanguage.current)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StoreStyle.background)
            .padding(horizontal = 12.dp)
            .padding(top = 34.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "SHOKTUK.COM",
            modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
            textAlign = TextAlign.End,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = StoreStyle.blue,
        )

        // Icon and title are one centred line; a long title shrinks instead of pushing the icon off screen.
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(text.icon, null, tint = StoreStyle.blue.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
                FitText(text.title.uppercase(locale), 30f, FontWeight.Black, StoreStyle.blue)
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(44.dp).height(4.dp).background(StoreStyle.blue.copy(alpha = 0.35f), CircleShape))
        }

        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().wrapContentHeight(Alignment.Top, unbounded = true)) {
            text.subtitle?.let { StoreCaption(it) }
            content()
        }
        Spacer(Modifier.weight(1f))
    }
}

/** One line that shrinks (down to half) instead of wrapping. */
@Composable
private fun FitText(string: String, size: Float, weight: FontWeight, color: Color) {
    val scale = remember(string) { if (string.length <= 14) 1f else (14f / string.length).coerceAtLeast(0.5f) }
    Text(
        string, fontSize = (size * scale).sp, fontWeight = weight, fontFamily = rounded, color = color,
        maxLines = 1, softWrap = false, overflow = TextOverflow.Clip, letterSpacing = 0.6.sp,
    )
}

@Composable
fun StoreCaption(string: String, modifier: Modifier = Modifier) {
    val locale = storeLocale(LocalStoreLanguage.current)
    Text(
        string.uppercase(locale),
        modifier = modifier.padding(horizontal = 8.dp),
        fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold, fontFamily = rounded,
        letterSpacing = 0.3.sp, color = StoreStyle.blue.copy(alpha = 0.9f), textAlign = TextAlign.Center, maxLines = 3,
    )
}

/**
 * A keyboard card: the text field with what was typed, and the real keyboard below it (see StoreKeyboardHost.kt).
 * [ringAt] (fractions of the keyboard picture) puts the blue ring there; a [StoreKeyboardConfig.glide] word draws the finger's trail.
 */
@Composable
fun StoreKeyboardCard(
    config: StoreKeyboardConfig,
    modifier: Modifier = Modifier,
    ringAt: Offset? = null,
    /** Puts the blue ring on the settings gear, wherever the real keyboard drew it. */
    ringOnGear: Boolean = false,
    /** Shows where the Auto Bitik switch lives: a small "gear > Auto Bitik (on)" tag in the card's top corner. */
    autoBitikHint: Boolean = false,
    /** 1 is the usual card width; a smaller factor makes a smaller card (for cards shown overlapping). */
    widthFactor: Float = 1f,
    /** Drawn over the card (inside it), e.g. the finger on the gear. */
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val context = LocalContext.current
    val config = config.copy(language = LocalStoreLanguage.current)
    var image by remember(config) { mutableStateOf<StoreKeyboardImage?>(null) }
    var failed by remember(config) { mutableStateOf<String?>(null) }
    if (LocalInspectionMode.current) {
        // Android Studio previews cannot run an input method service (there is no Android app process behind them), so the
        // keyboard itself is not drawn there: the layout, texts, cards and tilts are. The exported banners have the real keyboard.
        failed = "Real keyboard: shown in the exported banners (run _Editor/Store/make_store_screenshots.sh)"
    } else {
        LaunchedEffect(config) {
            image = StoreKeyboardSnapshotter.snapshot(context, config)
            if (image == null) failed = StoreKeyboardSnapshotter.lastError
        }
    }
    var pictureTop by remember { mutableStateOf(0f) }
    Box(modifier.fillMaxWidth(0.9f * widthFactor)) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(storeCardCorner)).background(StoreStyle.keyboardBackground)) {
    Column {
        config.typed?.let { typed ->
            Row(
                Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = if (autoBitikHint) 20.dp else 12.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(19.dp)).background(Color(0xFFD9D7E0)).padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    // Old Turkic is always Zamanchak (system fonts have no such glyphs).
                    val oldTurkic = typed.codePoints().anyMatch { it in 0x10C00..0x10C4F }
                    Text(
                        typed, fontSize = if (oldTurkic) 20.sp else 18.sp, color = Color(0xFF1C1B1F), maxLines = 1,
                        fontFamily = if (oldTurkic) FontFamily(Font(R.font.zamanchak)) else null,
                    )
                }
                Box(Modifier.size(38.dp).background(StoreStyle.send, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ArrowUpward, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
        val picture = image
        if (picture == null) {
            Box(Modifier.fillMaxWidth().aspectRatio(1.25f).padding(12.dp)) {
                failed?.let { Text(it, fontSize = 11.sp, color = Color(0xFF8B0000)) }
            }
        } else {
            val w = picture.bitmap.width
            val h = picture.bitmap.height
            Canvas(Modifier.fillMaxWidth().aspectRatio(w.toFloat() / h).onGloballyPositioned { pictureTop = it.positionInParent().y }) {
                drawImage(picture.bitmap, IntOffset.Zero, IntSize(w, h), IntOffset.Zero, IntSize(size.width.toInt(), size.height.toInt()))
                val scale = size.width / w
                config.glide?.let { word ->
                    val letters = word.lowercase().let { w -> (0 until w.codePointCount(0, w.length)).map { i -> w.substring(w.offsetByCodePoints(0, i), w.offsetByCodePoints(0, i + 1)) } }
                    storeTrail(letters.mapNotNull { picture.keys[it] }.map { Offset(it.x * scale, it.y * scale) })
                }
                ringAt?.let { storeRing(Offset(it.x * size.width, it.y * size.height)) }
            }
        }
    }
    if (autoBitikHint) StoreAutoBitikHint(Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 12.dp))
    }
    // The ring on the gear and the overlay (the finger) are drawn outside the clipped card, so nothing is cut at its edge.
    val gear = if (ringOnGear) image?.gear else null
    val ringPicture = image
    if (gear != null && ringPicture != null) {
        Canvas(Modifier.matchParentSize()) {
            val scale = size.width / ringPicture.bitmap.width
            storeRing(Offset(gear.x * scale, pictureTop + gear.y * scale))
        }
    }
    overlay()
    // The arrow sits outside the clipped card, level with the key row it points at (measured on the real keyboard picture).
    val picture = image
    val arrowRow = config.arrowTarget?.let { picture?.rows?.get(it) }
    if (picture != null && arrowRow != null) {
        BoxWithConstraints(Modifier.matchParentSize()) {
            val scale = constraints.maxWidth.toFloat() / picture.bitmap.width
            val size = 34.dp
            Icon(
                Icons.Filled.ArrowForward, null, tint = StoreStyle.blue,
                modifier = Modifier.align(Alignment.TopStart).size(size)
                    .offset { androidx.compose.ui.unit.IntOffset(-18.dp.roundToPx(), (pictureTop + arrowRow * scale - size.roundToPx() / 2f).toInt()) },
            )
        }
    }
    }
}

/** "Settings gear > Auto Bitik (on)": the way to the switch inside the keyboard's own settings (same tag as on iOS). */
@Composable
private fun StoreAutoBitikHint(modifier: Modifier = Modifier) {
    val language = LocalStoreLanguage.current
    val label = when { language.startsWith("ky") -> "Авто Битик"; language == "tr" -> "Otomatik Bitik"; else -> "Auto Bitik" }
    val blue = StoreStyle.blue
    Row(
        modifier.height(24.dp).background(Color.White, CircleShape).padding(start = 10.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(Icons.Filled.Settings, null, tint = blue, modifier = Modifier.size(14.dp))
        Icon(Icons.Filled.ChevronRight, null, tint = blue.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
        Icon(Icons.Filled.AutoAwesome, null, tint = blue, modifier = Modifier.size(14.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = blue, maxLines = 1)
        Box(Modifier.width(32.dp).height(20.dp), contentAlignment = Alignment.Center) {
            Switch(
                checked = true, onCheckedChange = null,
                modifier = Modifier.wrapContentSize(unbounded = true).scale(0.6f),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Color(0xFF34C759), checkedThumbColor = Color.White, checkedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

/**
 * A card showing a real screen of the app, live (the Compose screen itself, no picture): laid out at the phone's width and
 * shrunk to the card's width, like the iOS `StoreScreenCard`. [height] is in phone dp.
 */
@Composable
fun StoreScreenCard(height: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val language = LocalStoreLanguage.current
    // The screen inside speaks the banner's language.
    remember(language) {
        Language.entries.find { it.code == language }?.let { LocalizationManager.currentLanguage = it }
        language
    }
    val phoneWidth = LocalConfiguration.current.screenWidthDp.takeIf { it > 0 } ?: 411
    val cardWidth = phoneWidth * 0.9f
    val scale = cardWidth / phoneWidth
    Box(
        modifier.size(cardWidth.dp, (height.value * scale).dp).clip(RoundedCornerShape(storeCardCorner)).background(Color.White)
    ) {
        Box(
            Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).size(phoneWidth.dp, height)
                .graphicsLayer { scaleX = scale; scaleY = scale; transformOrigin = TransformOrigin(0f, 0f) }
        ) {
            ShoktukKeyboardTheme(darkTheme = false, dynamicColor = false) {
                // The app's section colours are dark plum and slate; the banner shows them as the light grey cards iOS has.
                val light = Color(0xFFEDEBF3)
                MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(
                    tertiary = light, onTertiary = Color(0xFF1C1B1F), secondary = light, onSecondary = Color(0xFF1C1B1F),
                )) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() }
                }
            }
        }
    }
}

/** A card turned a few degrees, leaning out of the banner, with a soft shadow. */
fun Modifier.storeTilted(degrees: Float, x: Float = 0f, y: Float = 0f): Modifier =
    this.graphicsLayer {
        rotationZ = degrees
        translationX = x * density
        translationY = y * density
        shadowElevation = 14f * density
        shape = RoundedCornerShape(storeCardCorner)
        clip = false
        ambientShadowColor = Color.Black.copy(alpha = 0.3f)
        spotShadowColor = Color.Black.copy(alpha = 0.3f)
    }

/** The blue ring that marks the key to tap (same look as the iOS one). */
fun androidx.compose.ui.graphics.drawscope.DrawScope.storeRing(center: Offset) {
    val outer = 14.dp.toPx()
    drawCircle(Color(0xFF0A84FF).copy(alpha = 0.18f), outer, center)
    drawCircle(Color(0xFF0A84FF), outer, center, style = Stroke(width = 3.dp.toPx()))
    drawCircle(Color(0xFF0A84FF).copy(alpha = 0.55f), outer - 4.dp.toPx(), center, style = Stroke(width = 2.dp.toPx()))
}

/** The glide trail: translucent blue, round caps, a dot at the fingertip. */
fun androidx.compose.ui.graphics.drawscope.DrawScope.storeTrail(points: List<Offset>) {
    if (points.size < 2) return
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val p0 = points[(i - 2).coerceAtLeast(0)]; val p1 = points[i - 1]; val p2 = points[i]; val p3 = points[(i + 1).coerceAtMost(points.size - 1)]
            cubicTo(
                p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
                p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
                p2.x, p2.y,
            )
        }
    }
    drawPath(path, Color(0xFF0A84FF).copy(alpha = 0.55f), style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawCircle(Color(0xFF0A84FF).copy(alpha = 0.25f), 16.dp.toPx(), points.last())
}
