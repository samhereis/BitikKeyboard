package com.shoktuk.shoktukkeyboard

import BasicInfo_Screen
import ChangeLanguageWidget
import HowToEnable_Screen
import ModernizedTamgasView
import OriginalTamgasView
import SideMenuHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shoktuk.shoktukkeyboard.project.data.BasicInfoScreens
import com.shoktuk.shoktukkeyboard.project.data.MainScreens
import com.shoktuk.shoktukkeyboard.project.data.SideMenuItem
import com.shoktuk.shoktukkeyboard.project.screens.settings.SettingsScreen
import com.shoktuk.shoktukkeyboard.project.screens.testKeyboard.TestKeyboard_Screen
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import kotlinx.coroutines.launch
import localized

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideMenuView() {
    val drawerState = rememberDrawerState(DrawerValue.Open)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val mainScreens = listOf(
        MainScreens.HOW_TO_ENABLE.id, MainScreens.TEST_KEYBOARD.id, MainScreens.BASIC_INFO.id, MainScreens.SETTINGS.id
    )

    // Drawer items – these are your main screen routes.
    val items = listOf(
        SideMenuItem(MainScreens.HOW_TO_ENABLE, MainScreens.HOW_TO_ENABLE.systemImageName),
        SideMenuItem(MainScreens.TEST_KEYBOARD, MainScreens.TEST_KEYBOARD.systemImageName),
        SideMenuItem(MainScreens.BASIC_INFO, MainScreens.BASIC_INFO.systemImageName),
        SideMenuItem(MainScreens.SETTINGS, MainScreens.SETTINGS.systemImageName)
    )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: MainScreens.HOW_TO_ENABLE.id

    val isMainScreen = mainScreens.contains(currentRoute)

    ModalNavigationDrawer(
        drawerState = drawerState, gesturesEnabled = isMainScreen, drawerContent = {
            ModalDrawerSheet {
                Column {
                    SideMenuHeader(
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .padding(20.dp)
                    )
                }
                // Drawer items.
                items.forEachIndexed { index, drawerItem ->
                    NavigationDrawerItem(
                        selected = selectedItemIndex == index, onClick = {
                            selectedItemIndex = index
                            scope.launch { drawerState.close() }
                            val route = when (selectedItemIndex) {
                                0 -> MainScreens.HOW_TO_ENABLE.id
                                1 -> MainScreens.TEST_KEYBOARD.id
                                2 -> MainScreens.BASIC_INFO.id
                                3 -> MainScreens.SETTINGS.id
                                else -> MainScreens.HOW_TO_ENABLE.id
                            }
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }, icon = {
                            Row(
                                modifier = Modifier, horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = drawerItem.icon, contentDescription = null, modifier = Modifier.padding(10.dp)
                                )
                                Text(text = drawerItem.path.title.localized("loc_sideBar", context))
                            }
                        }, label = {}, modifier = Modifier.padding(5.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                //ChangeLanguageWidget()
                Spacer(modifier = Modifier.weight(1f))
            }
        }) {
        Scaffold(
            topBar = {
                if (isMainScreen) {
                    TopAppBar(
                        title = {
                            val menuTitle = items.find { it.path.id == currentRoute }?.path?.title?.localized("loc_sideBar", context) ?: ""
                            Text(text = menuTitle, fontSize = 15.sp, color = Color.White)
                        }, navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu, contentDescription = "Menu", tint = Color.White
                                )
                            }
                        }, colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.background)
                    )
                } else {
                    TopAppBar(
                        title = {
                            val menuTitle = BasicInfoScreens.entries.find { it.id == currentRoute }?.title?.localized("loc_basicInfo", context) ?: ""
                            Text(text = menuTitle, fontSize = 15.sp, color = Color.White)
                        }, navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White
                                )
                            }
                        }, colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.background)
                    )
                }
            }) { innerPadding ->
            NavHost(
                navController = navController, startDestination = MainScreens.HOW_TO_ENABLE.id, modifier = Modifier.padding(innerPadding)
            ) {
                composable(MainScreens.HOW_TO_ENABLE.id) { HowToEnable_Screen() }
                composable(MainScreens.TEST_KEYBOARD.id) { TestKeyboard_Screen() }
                composable(MainScreens.BASIC_INFO.id) { BasicInfo_Screen(navController) }
                composable(MainScreens.SETTINGS.id) { SettingsScreen() }

                composable(BasicInfoScreens.ORIGINAL_TAMGAS.id) { OriginalTamgasView() }
                composable(BasicInfoScreens.MODERNIZED_TAMGAS.id) { ModernizedTamgasView() }
                composable(BasicInfoScreens.RULES_OF_WRITING.id) { RulesOfWritingView() }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SideMenuViewPreview() {
    ShoktukKeyboardTheme {
        SideMenuView()
    }
}
