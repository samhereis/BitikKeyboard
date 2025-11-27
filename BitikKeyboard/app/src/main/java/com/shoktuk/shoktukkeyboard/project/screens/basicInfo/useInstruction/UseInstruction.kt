package com.shoktuk.bittik.instructions

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_UsingTheKeyboard
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseInstruction() {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp), horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = Loc_UsingTheKeyboard.keyboardDesign.localizedTitle(LocalContext.current), style = MaterialTheme.typography.bodyLarge
        )

        Image(
            painter = painterResource(id = R.drawable.qwerty_keyboard),
            contentDescription = "QWERTY Keyboard",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.FillWidth
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = Loc_UsingTheKeyboard.shiftButton.localizedTitle(LocalContext.current),
            style = MaterialTheme.typography.bodyLarge
        )

        VideoPlay(videoName = "keyboardusing_demo")

        Spacer(Modifier.height(16.dp))

        SectionCard {
            Column {
                Text(Loc_UsingTheKeyboard.standartColorsConsonants.localizedTitle(LocalContext.current))
                Text(Loc_UsingTheKeyboard.blueColorIsVowels.localizedTitle(LocalContext.current), color = Color(0xFF5875FF))
                Text(Loc_UsingTheKeyboard.redIsSpecialLetters.localizedTitle(LocalContext.current), color = Color(0xFFC43494)) // orange
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text(Loc_UsingTheKeyboard.disablingTheColoring.localizedTitle(LocalContext.current))
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = Loc_UsingTheKeyboard.secondLetterVariantsOnHold.localizedTitle(LocalContext.current), style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(6.dp))

        InfoCard(
            infoTexts = listOf(
                Loc_UsingTheKeyboard.noNeedToChangeShiftInOneWord.localizedTitle(LocalContext.current),
                "",
                Loc_UsingTheKeyboard.needToChangeShiftException.localizedTitle(LocalContext.current),
                "",
                "𐰶𐰺𐰏𐰃𐰕 🚫",
                "𐰶𐰺𐰍𐰃𐰕 ✅",
                "",
                "𐰢𐰅𐰣 𐰳𐰀𐰕𐰓𐰃𐰢 🚫",
                "𐰢𐰤 𐰳𐰕𐰑𐰃𐰢 ✅"
            )
        )

        Spacer(Modifier.height(24.dp))

        Text(Loc_UsingTheKeyboard.advice.localizedTitle(LocalContext.current), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(6.dp))

        Text(
            text = Loc_UsingTheKeyboard.youCanSeeWordWritingInLatinOrKiril.localizedTitle(LocalContext.current), style = MaterialTheme.typography.bodyLarge
        )

        VideoPlay(videoName = "keyboardusing_alphabet_demo")

        Spacer(Modifier.height(32.dp))
    }
}

/* ---------- Support Components ---------- */

@Composable
fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun InfoCard(infoTexts: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), tonalElevation = 2.dp, shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(8.dp))
            infoTexts.forEach {
                Text(
                    text = it, style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun VideoPlay(videoName: String) {
    val context = LocalContext.current
    val aspect = remember(videoName) { getVideoAspectRatio(context, videoName) }

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(rawUri(ctx, videoName))
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    start()
                }
            }
        }, update = { view ->
            if (!view.isPlaying) view.start()
        }, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(25.dp)) // ✅ Rounded corners
    )
}

fun rawUri(context: android.content.Context, resName: String): Uri {
    val id = context.resources.getIdentifier(resName, "raw", context.packageName)
    return Uri.parse("android.resource://${context.packageName}/$id")
}

/** Returns width/height ratio, accounting for rotation */
fun getVideoAspectRatio(context: android.content.Context, resName: String): Float {
    val retriever = MediaMetadataRetriever()
    return try {
        val uri = rawUri(context, resName)
        retriever.setDataSource(context, uri)
        val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toFloatOrNull() ?: 16f
        val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toFloatOrNull() ?: 9f
        val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
        val (width, height) = if (rotation == 90 || rotation == 270) h to w else w to h
        (width / height).takeIf { it > 0 } ?: 16f / 9f
    } catch (_: Throwable) {
        16f / 9f
    } finally {
        try {
            retriever.release()
        } catch (_: Throwable) {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BasicInfo_ScreenPreview() {

    ShoktukKeyboardTheme {
        UseInstruction()
    }
}