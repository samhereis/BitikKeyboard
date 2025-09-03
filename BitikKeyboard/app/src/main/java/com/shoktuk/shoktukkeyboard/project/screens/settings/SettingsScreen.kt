package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shoktuk.shoktukkeyboard.project.data.AS_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.A_Letter_Variannt
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.EB_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.EN_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.ESH_Letter_Variant
import com.shoktuk.shoktukkeyboard.project.data.E_Letter_Variannt
import com.shoktuk.shoktukkeyboard.project.data.KeyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager
import com.shoktuk.shoktukkeyboard.project.data.TamgaTranscription
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import localized

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var keyboardVariant by remember { mutableStateOf(SettingsManager.getKeyboardVariant(context)) }
    var bitikDialect by remember { mutableStateOf(SettingsManager.getBitikDialect(context)) }
    var textTranscription by remember { mutableStateOf(SettingsManager.getTextTranscription(context)) }
    var letterTranscription by remember { mutableStateOf(SettingsManager.getLeterTranscription(context)) }

    var aVariant by remember { mutableStateOf(SettingsManager.getA_Variant(context)) }
    var eVariant by remember { mutableStateOf(SettingsManager.getE_Variant(context)) }
    var ebVariant by remember { mutableStateOf(SettingsManager.getEB_Variant(context)) }
    var eNariant by remember { mutableStateOf(SettingsManager.getEN_Variant(context)) }
    var asVariant by remember { mutableStateOf(SettingsManager.getAS_Variant(context)) }
    var eshVariant by remember { mutableStateOf(SettingsManager.getES_Variant(context)) }

// in SettingsScreen() Column modifier:
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),   // ✅ real scroll
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CenteredDropdownPopup(
            label = "Битик түрү".localized("loc_settings", context), options = KeyboardVariant.entries, selected = keyboardVariant, onSelect = { variant ->
                keyboardVariant = variant
                SettingsManager.setKeyboardVariant(context, variant)
            }, optionLabel = { it.id.localized("loc_settings", context) }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "Битик диалект", options = BitikDialect.entries, selected = bitikDialect, onSelect = { alpha ->
                bitikDialect = alpha
                SettingsManager.setBitikDialect(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "Жазуу транскрипция", options = TextTranscription.entries, selected = textTranscription, onSelect = { alpha ->
                textTranscription = alpha
                SettingsManager.setTextTranscription(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "Тамга транскрипция", options = TamgaTranscription.entries, selected = letterTranscription, onSelect = { alpha ->
                letterTranscription = alpha
                SettingsManager.setLeterTranscription(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        CenteredDropdownPopup(
            label = "A тамга", options = A_Letter_Variannt.entries, selected = aVariant, onSelect = { alpha ->
                aVariant = alpha
                SettingsManager.setAVariant(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "Э тамга", options = E_Letter_Variannt.entries, selected = eVariant, onSelect = { alpha ->
                eVariant = alpha
                SettingsManager.setEVariant(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "эБ тамга", options = EB_Letter_Variant.entries, selected = ebVariant, onSelect = { alpha ->
                ebVariant = alpha
                SettingsManager.setEB_Variant(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        CenteredDropdownPopup(
            label = "эН тамга", options = EN_Letter_Variant.entries, selected = eNariant, onSelect = { alpha ->
                eNariant = alpha
                SettingsManager.setEN_Variant(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        Text("Алтай варианты үчүн:")
        CenteredDropdownPopup(
            label = "аС тамга", options = AS_Letter_Variant.entries, selected = asVariant, onSelect = { alpha ->
                asVariant = alpha
                SettingsManager.setAS_Variant(context, alpha)
            }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
        )

        if (SettingsManager.getKeyboardVariant(context) == KeyboardVariant.SAMAGAN) {
            CenteredDropdownPopup(
                label = "эШ тамга", options = ESH_Letter_Variant.entries, selected = eshVariant, onSelect = { alpha ->
                    eshVariant = alpha
                    SettingsManager.setES_Variant(context, alpha)
                }, optionLabel = { it.id }, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ShoktukKeyboardTheme {
        SettingsScreen()
    }
}

@Composable
fun <T> CenteredDropdownPopup(
    modifier: Modifier = Modifier,
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String = { it.toString() }
) {
    var showPopup by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp) // text-field-like height
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: label (non-clickable)
            Text(
                label,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            // RIGHT: value + chevron (clickable, uses default M3 ripple)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp)) // keeps ripple nicely bounded
                    .clickable { showPopup = true }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(optionLabel(selected), fontSize = 15.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
    }

    if (showPopup) {
        Dialog(onDismissRequest = { showPopup = false }) {
            Card(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .heightIn(max = 520.dp)
            ) {
                // Optional header
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(label, style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    items(options.size) { idx ->
                        val option = options[idx]
                        val isSel = option == selected

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option)
                                    showPopup = false
                                }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSel, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                optionLabel(option),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSel) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (idx != options.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }
}