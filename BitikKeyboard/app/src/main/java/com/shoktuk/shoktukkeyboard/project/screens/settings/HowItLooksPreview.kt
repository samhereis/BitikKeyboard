package com.shoktuk.shoktukkeyboard.project.screens.settings

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

/** Mirrors the iOS "SettingImages/[settingsKey]/[settingsKey]_[index]" asset naming convention. */
fun <T : Enum<T>> T.settingImageName(settingsKey: String): String {
    return "${settingsKey.lowercase()}_${this.ordinal}"
}

private const val PREVIEW_ASPECT_RATIO = 1170f / 653f

/** Mirrors iOS's `settingPreviewAspectRatio(named:)` — reads the real image dimensions (just the
 *  header, not a full decode) so previews size to their actual aspect ratio instead of being
 *  squished into a fixed box. Falls back to the same default ratio iOS uses when the asset is missing. */
private fun settingPreviewAspectRatio(context: Context, imageName: String): Float {
    val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
    if (resId == 0) return PREVIEW_ASPECT_RATIO

    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(context.resources, resId, options)
    return if (options.outWidth > 0 && options.outHeight > 0) {
        options.outWidth.toFloat() / options.outHeight.toFloat()
    } else {
        PREVIEW_ASPECT_RATIO
    }
}

@Composable
fun SettingPreviewImage(imageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resId = remember(imageName) {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    if (resId != 0) {
        Image(
            painter = painterResource(id = resId), contentDescription = null, modifier = modifier, contentScale = ContentScale.Fit
        )
    } else {
        Column(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.BrokenImage, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp)
            )
            Text(
                "Missing asset\n\"$imageName\"",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> HowItLooksPreviewRow(
    items: List<T>,
    selection: T?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    label: (T) -> String,
    imageName: (T) -> String,
    headerLabel: String = Loc_Settings.howItLooks.localizedTitle(LocalContext.current)
) {
    var fullListItem by remember { mutableStateOf<T?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!expanded) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                headerLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        if (expanded) {
            val context = LocalContext.current
            val thumbnailWidth = 200.dp
            val thumbnailHeight = items
                .map { thumbnailWidth / settingPreviewAspectRatio(context, imageName(it)) }
                .maxOrNull() ?: (thumbnailWidth / PREVIEW_ASPECT_RATIO)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items.forEach { item ->
                    Column(
                        modifier = Modifier.clickable { fullListItem = item }, verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(label(item), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        val isSelected = item == selection
                        SettingPreviewImage(
                            imageName = imageName(item),
                            modifier = Modifier
                                .size(width = thumbnailWidth, height = thumbnailHeight)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .then(
                                    if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                        )
                    }
                }
            }
        }
    }

    val openItem = fullListItem
    if (openItem != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { fullListItem = null }, sheetState = sheetState) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(items) { item ->
                    val context = LocalContext.current
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isSelected = item == selection
                        Text(
                            label(item),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        SettingPreviewImage(
                            imageName = imageName(item),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(settingPreviewAspectRatio(context, imageName(item)))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                    else Modifier
                                )
                        )
                    }
                }
            }
        }
    }
}

/** Live tuning ground for the gap between a setting row and its "How it looks" — tweak the
 *  paddings in CenteredDropdownPopup.kt / EnumSwitchSetting.kt and hit refresh here. */
@Preview(name = "Setting + How it looks (collapsed)", showBackground = true)
@Composable
private fun HowItLooksSpacingPreview_Collapsed() {
    ShoktukKeyboardTheme {
        var expanded by remember { mutableStateOf(false) }
        Column(Modifier.padding(16.dp)) {
            EnumSwitchSetting(
                label = "Coloring",
                selected = true,
                optionOn = true,
                optionOff = false,
                onSelect = {},
                onLabelClick = { expanded = !expanded },
                extraContent = {
                    HowItLooksPreviewRow(
                        items = listOf(true, false),
                        selection = true,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        label = { if (it) "On" else "Off" },
                        imageName = { if (it) "coloring_0" else "coloring_1" }
                    )
                }
            )
        }
    }
}

@Preview(name = "Setting + How it looks (expanded)", showBackground = true)
@Composable
private fun HowItLooksSpacingPreview_Expanded() {
    ShoktukKeyboardTheme {
        var expanded by remember { mutableStateOf(true) }
        Column(Modifier.padding(16.dp)) {
            EnumSwitchSetting(
                label = "Coloring",
                selected = true,
                optionOn = true,
                optionOff = false,
                onSelect = {},
                onLabelClick = { expanded = !expanded },
                extraContent = {
                    HowItLooksPreviewRow(
                        items = listOf(true, false),
                        selection = true,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        label = { if (it) "On" else "Off" },
                        imageName = { if (it) "coloring_0" else "coloring_1" }
                    )
                }
            )
        }
    }
}

