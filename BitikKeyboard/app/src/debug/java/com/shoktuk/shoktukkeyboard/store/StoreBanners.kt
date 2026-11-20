package com.shoktuk.shoktukkeyboard.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import BasicInfo_Screen
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.textTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.letterTranscription
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.TextTranscription
import com.shoktuk.shoktukkeyboard.project.data.LetterTranscription
import androidx.navigation.compose.rememberNavController
import com.shoktuk.shoktukkeyboard.project.screens.settings.MainSettingsScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.keyboard.KeyboardMode
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem

/** The banner names are the same as on iOS (`_Editor/<Platform>_<lang>/<Platform>_<lang>_<Name>.png`). */
val storeBannerNames = listOf("AndMore", "ConvertToBitik", "Emojis", "Manual", "OtherAlphabets", "Savables", "Settings")

private fun pick(language: String, en: StoreText, ky: StoreText, tr: StoreText): StoreText = when {
    language.startsWith("ky") -> ky
    language == "tr" -> tr
    else -> en
}

@Composable
fun StoreBanner(name: String) {
    val language = LocalStoreLanguage.current
    val kiril = language.startsWith("ky")
    when (name) {
        "AndMore" -> {
            val text = pick(
                language,
                StoreText(Icons.Filled.MoreHoriz, "And more", "Glide typing!", listOf("Quick settings are one tap away")),
                StoreText(Icons.Filled.MoreHoriz, "Дагы көп нерсе!", "Сызып жазуу (Т9)!", listOf("Тез жөндөөлөр ар дайым жакында")),
                StoreText(Icons.Filled.MoreHoriz, "Ve daha fazlası", "Kaydırarak yazma: parmağınızı harflerin üzerinde gezdirerek kelime yazın", listOf("Hızlı ayarlar tek dokunuş uzağınızda")),
            )
            StoreBannerFrame(text) {
                val alphabet = if (kiril) WritingSystem.Kiril else WritingSystem.Latin
                val word = if (kiril) "Жаз" else "Jaz"
                StoreKeyboardCard(StoreKeyboardConfig(alphabet, typed = word, glide = word))
                Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.TouchApp, null, tint = StoreStyle.blue, modifier = Modifier.size(28.dp))
                    StoreCaption(text.captions[0])
                }
                // The way to the quick settings: the finger taps the gear on the keyboard, and the panel it opens lies over the keys.
                // Both cards are whole and run past the banner's edges, each turned a few degrees (same as the iOS banner).
                Box(Modifier.fillMaxWidth().height(335.dp)) {
                    StoreKeyboardCard(
                        StoreKeyboardConfig(alphabet, typed = null),
                        Modifier.align(Alignment.TopStart).offset(x = (-10).dp).storeTilted(-4f),
                        ringOnGear = true, widthFactor = 0.8f,
                        overlay = {
                            // Mirrored, so its fingertip touches the gear from the lower left and the gear stays visible.
                            Icon(
                                Icons.Filled.TouchApp, null, tint = StoreStyle.blue,
                                modifier = Modifier.align(Alignment.TopEnd).padding(top = 13.dp, end = 33.dp).size(48.dp).graphicsLayer { scaleX = -1f; transformOrigin = TransformOrigin(0.62f, 0.08f); rotationZ = 18f },
                            )
                        },
                    )
                    StoreKeyboardCard(
                        StoreKeyboardConfig(alphabet, panel = KeyboardMode.Settings, typed = null),
                        Modifier.align(Alignment.TopStart).offset(x = 80.dp, y = 100.dp).storeTilted(3f),
                        widthFactor = 0.85f,
                    )
                }
            }
        }

        "ConvertToBitik" -> {
            val text = pick(
                language,
                StoreText(Icons.Filled.Autorenew, "Convert to Bitik", "No need to know Bitik - Just convert! To paste the converted text, just click on it", listOf("In “Auto Bitik” mode it converts as you write")),
                StoreText(Icons.Filled.Autorenew, "Битикке оодаруу", "Битикти билбей да жазыңыз! Чаптоо үчүн оодарылган сөздү басыңыз", listOf("«Авто Битик» режими жазып жатканыңызда өзү оодарып берет")),
                StoreText(Icons.Filled.Autorenew, "Bitik'e çevir", "Bitik bilmeniz gerekmez! Çevirin, yapıştırmak için basılı tutun", listOf("“Bitik” modunda yazarken kendisi çevirir")),
            )
            StoreBannerFrame(text) {
                StoreKeyboardCard(StoreKeyboardConfig(WritingSystem.Latin, typed = "Basqıç"))
                StoreCaption(text.captions[0], Modifier.padding(top = 14.dp))
                StoreKeyboardCard(StoreKeyboardConfig(WritingSystem.Kiril, typed = "Жазуу", bitikMode = true), autoBitikHint = true)
            }
        }

        "Emojis" -> {
            val text = pick(
                language,
                StoreText(Icons.Filled.EmojiEmotions, "Emojis", "Most used ones are on top", listOf("You can open emojis by holding on symbols button")),
                StoreText(Icons.Filled.EmojiEmotions, "Эможилер", "Жыш колдонулгандар жогоруда жайгашат", listOf("Ачуу үчүн символдор баскычын узак басыңыз")),
                StoreText(Icons.Filled.EmojiEmotions, "Emojiler", "En çok kullanılanlar üstte", listOf("Emojileri semboller tuşuna basılı tutarak açabilirsiniz")),
            )
            StoreBannerFrame(text) {
                StoreKeyboardCard(StoreKeyboardConfig(panel = KeyboardMode.Emojis, typed = null))
                StoreCaption(text.captions[0], Modifier.padding(top = 14.dp))
                StoreKeyboardCard(StoreKeyboardConfig(typed = "𐰖𐰁𐰕𐰩𐰩", letterTranscription = false, wordSeparator = WordSeparator.Off, variant = BitikVariant.SAMAGAN, arrowTarget = StoreArrowTarget.BottomRow))
            }
        }

        "Manual" -> {
            val text = pick(
                language,
                StoreText(Icons.Filled.MenuBook, "Manual", "Bitik manual, rules and letter memorization"),
                StoreText(Icons.Filled.MenuBook, "Нускама", "Битик боюнча нускама, эрежелер жана тамгаларды жаттоо"),
                StoreText(Icons.Filled.MenuBook, "Kılavuz", "Bitik kılavuzu, kurallar ve harfleri ezberleme"),
            )
            StoreBannerFrame(text) { StoreScreenCard(600.dp) { BasicInfo_Screen(rememberNavController()) } }
        }

        "OtherAlphabets" -> {
            val text = pick(
                language,
                StoreText(Icons.Filled.Translate, "More alphabets", "Latin, Cyrillic and Arab alphabets", listOf("Turn them on in the “How to enable” page")),
                StoreText(Icons.Filled.Translate, "Башка ариптер", "Латын, кирилл жана араб ариптери", listOf("«Орнотуу ыкмасы» барагынан күйгүзүңүз")),
                StoreText(Icons.Filled.Translate, "Diğer alfabeler", "Latin, Kiril ve Arap alfabeleri", listOf("“Nasıl etkinleştirilir” sayfasından açın")),
            )
            StoreBannerFrame(text) {
                StoreKeyboardCard(StoreKeyboardConfig(WritingSystem.Latin, typed = "Latınça", coloring = false))
                StoreCaption(text.captions[0], Modifier.padding(top = 14.dp))
                StoreKeyboardCard(StoreKeyboardConfig(WritingSystem.Arab, typed = "شوقتۇق"))
            }
        }

        "Savables" -> {
            val text = pick(
                language,
                StoreText(Icons.Filled.AttachFile, "Savables", "You can use savables for most used words", listOf("Opens on Shift hold. Edit them in settings")),
                StoreText(Icons.Filled.AttachFile, "Сакталмалар", "Көп колдонулган сөздөр үчүн сакталмаларды колдонсоңуз болот", listOf("Shift баскычын басып кармаганда ачылат. Жөндөөлөрдөн түзөтүңүз")),
                StoreText(Icons.Filled.AttachFile, "Kayıtlılar", "En çok kullanılan kelimeler için kayıtlıları kullanabilirsiniz", listOf("Shift'e basılı tutunca açılır. Ayarlardan düzenleyin")),
            )
            StoreBannerFrame(text) {
                StoreKeyboardCard(StoreKeyboardConfig(panel = KeyboardMode.SavedStrings, typed = null, wordSeparator = WordSeparator.Off))
                StoreCaption(text.captions[0], Modifier.padding(top = 14.dp))
                StoreKeyboardCard(
                    StoreKeyboardConfig(
                        typed = "𐰅𐰏𐰅𐰢𐰅𐰥𐰓𐰄𐰚", letterTranscription = false, coloring = false, shift = true, enCut = true,
                        wordSeparator = WordSeparator.Off, variant = BitikVariant.SAMAGAN, arrowTarget = StoreArrowTarget.LastLetterRow,
                    )
                )
            }
        }

        "Settings" -> {
            val text = pick(
                language,
                StoreText(Icons.Filled.Tune, "Settings", "You can personalize the keyboard"),
                StoreText(Icons.Filled.Tune, "Жөндөөлөр", "Баскычтопту өзүңүзгө ылайыктай аласыз"),
                StoreText(Icons.Filled.Tune, "Ayarlar", "Klavyeyi kişiselleştirebilirsiniz"),
            )
            val context = LocalContext.current
            // The values the screen shows (the same as the iOS banner): not whatever the last keyboard card left in the saved settings.
            remember(context) {
                context.keyboardVariant = BitikVariant.Modern
                context.textTranscription = TextTranscription.On
                context.letterTranscription = LetterTranscription.On
                context.wordSeparator = WordSeparator.Off
                true
            }
            StoreBannerFrame(text) { StoreScreenCard(720.dp) { MainSettingsScreen(showSliders = false, showVariantNote = false) } }
        }
    }
}
