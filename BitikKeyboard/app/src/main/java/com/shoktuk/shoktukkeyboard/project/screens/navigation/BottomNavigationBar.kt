package com.shoktuk.shoktukkeyboard.project.screens.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_SideMenu
import localized

@Composable
fun BottomNavigationBar(
    currentRoute: String, onNavigationItemSelected: (MainTabs) -> Unit, modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    NavigationBar(modifier = modifier.fillMaxWidth()) {
        MainTabs.entries.forEach { tab ->
            val selected = currentRoute == tab.route

            NavigationBarItem(
                selected = selected, onClick = { onNavigationItemSelected(tab) }, icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = when (tab) {
                                MainTabs.HOW_TO_ENABLE -> Icons.Default.Info
                                MainTabs.TEST_KEYBOARD -> Icons.Default.Keyboard
                                MainTabs.BASIC_INFO -> Icons.Default.Book
                                MainTabs.SETTINGS -> Icons.Default.Settings
                            }, contentDescription = null
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                    }
                }, label = {
                    Text(
                        text = when (tab) {
                            MainTabs.HOW_TO_ENABLE -> Loc_SideMenu.HOW_TO_ENABLE.titleKey.localized("sideBar", context)
                            MainTabs.TEST_KEYBOARD -> Loc_SideMenu.TEST_KEYBOARD.titleKey.localized("sideBar", context)
                            MainTabs.BASIC_INFO -> Loc_SideMenu.BASIC_INFO.titleKey.localized("sideBar", context)
                            MainTabs.SETTINGS -> Loc_SideMenu.SETTINGS.titleKey.localized("sideBar", context)
                        }, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 2
                    )
                }, alwaysShowLabel = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    val currentRoute = remember { mutableStateOf(MainTabs.HOW_TO_ENABLE.route) }

    Surface {
        BottomNavigationBar(
            currentRoute = currentRoute.value, onNavigationItemSelected = { tab ->
                currentRoute.value = tab.route
            })
    }
}