package com.shoktuk.shoktukkeyboard.store

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

/*
 * Android Studio previews of the banners, three languages each (en, ky_kiril, tr): the layout, texts, cards and tilts are real;
 * the keyboard inside a card is a placeholder there, because previews cannot run an input method service. The banner files
 * (with the real keyboard) come from ScreenshotExporter.kt or `_Editor/Store/make_store_screenshots.sh`, run on a device.
 */

class StoreLanguages : PreviewParameterProvider<String> {
    override val values = sequenceOf("en", "ky_kiril", "tr")
}

private const val W = 411
private const val H = 914

@Composable
private fun Banner(name: String, language: String) =
    CompositionLocalProvider(LocalStoreLanguage provides language) { StoreBanner(name) }

@Preview(widthDp = W, heightDp = H, name = "AndMore") @Composable fun Preview_AndMore(@PreviewParameter(StoreLanguages::class) l: String) = Banner("AndMore", l)
@Preview(widthDp = W, heightDp = H, name = "ConvertToBitik") @Composable fun Preview_ConvertToBitik(@PreviewParameter(StoreLanguages::class) l: String) = Banner("ConvertToBitik", l)
@Preview(widthDp = W, heightDp = H, name = "Emojis") @Composable fun Preview_Emojis(@PreviewParameter(StoreLanguages::class) l: String) = Banner("Emojis", l)
@Preview(widthDp = W, heightDp = H, name = "Manual") @Composable fun Preview_Manual(@PreviewParameter(StoreLanguages::class) l: String) = Banner("Manual", l)
@Preview(widthDp = W, heightDp = H, name = "OtherAlphabets") @Composable fun Preview_OtherAlphabets(@PreviewParameter(StoreLanguages::class) l: String) = Banner("OtherAlphabets", l)
@Preview(widthDp = W, heightDp = H, name = "Savables") @Composable fun Preview_Savables(@PreviewParameter(StoreLanguages::class) l: String) = Banner("Savables", l)
@Preview(widthDp = W, heightDp = H, name = "Settings") @Composable fun Preview_Settings(@PreviewParameter(StoreLanguages::class) l: String) = Banner("Settings", l)
