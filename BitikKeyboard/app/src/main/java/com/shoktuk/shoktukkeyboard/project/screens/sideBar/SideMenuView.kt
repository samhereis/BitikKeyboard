package com.shoktuk.shoktukkeyboard

import BasicInfo_Screen
import HowToEnable_Screen
import ModernizedTamgasView
import OriginalTamgasView
import SideMenuHeader
import SupportScreen
import android.content.pm.PackageInfo
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.shoktuk.shoktukkeyboard.project.data.SettingScreens
import com.shoktuk.shoktukkeyboard.project.data.SideMenuItem
import com.shoktuk.shoktukkeyboard.project.screens.settings.SavedStringsScreen
import com.shoktuk.shoktukkeyboard.project.screens.settings.SettingsScreen
import com.shoktuk.shoktukkeyboard.project.screens.testKeyboard.TestKeyboard_Screen
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import localized

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideMenuView() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val context = LocalContext.current

    val items = MainScreens.entries.map { SideMenuItem(it, it.systemImageName) }
    val mainRoutes = MainScreens.entries.map { it.id }.toSet()

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: MainScreens.HOW_TO_ENABLE.id
    val isMainScreen = currentRoute in mainRoutes

    ModalNavigationDrawer(
        drawerState = drawerState, gesturesEnabled = isMainScreen, drawerContent = {
            ModalDrawerSheet {
                Column {
                    SideMenuHeader(
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .padding(bottom = 10.dp)
                    )
                }
                items.forEach { drawerItem ->
                    val selected = currentRoute == drawerItem.path.id
                    NavigationDrawerItem(
                        selected = selected, onClick = {
                        navigateRoot(scope, drawerState, navController, drawerItem.path.id)
                    }, icon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start
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
                AppVersionText(modifier = Modifier.padding(bottom = 25.dp))
            }
        }) {
        Scaffold(
            topBar = {
                if (isMainScreen) {
                    TopAppBar(
                        title = {
                        val menuTitle = MainScreens.entries.firstOrNull { it.id == currentRoute }?.title?.localized("loc_sideBar", context) ?: ""
                        Text(text = menuTitle, fontSize = 15.sp)
                    }, navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.background)
                    )
                } else {
                    TopAppBar(
                        title = {
                        val title = BasicInfoScreens.entries.firstOrNull { it.id == currentRoute }?.title?.localized("loc_basicInfo", context)
                            ?: SettingScreens.entries.firstOrNull { it.id == currentRoute }?.title?.localized("loc_settings", context) ?: ""
                        Text(text = title, fontSize = 15.sp)
                    }, navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
                composable(MainScreens.SETTINGS.id) {
                    SettingsScreen(
                        onOpenSavedStrings = { navController.navigate(SettingScreens.SavedStrings.id) })
                }
                composable(MainScreens.SUPPORT.id) { SupportScreen() }

                composable(BasicInfoScreens.ORIGINAL_TAMGAS.id) { OriginalTamgasView() }
                composable(BasicInfoScreens.MODERNIZED_TAMGAS.id) { ModernizedTamgasView() }
                composable(BasicInfoScreens.RULES_OF_WRITING.id) { RulesOfWritingView() }

                composable(SettingScreens.SavedStrings.id) { SavedStringsScreen() }
            }
        }
    }
}

private fun navigateRoot(
    scope: CoroutineScope, drawerState: androidx.compose.material3.DrawerState, navController: androidx.navigation.NavHostController, route: String
) {
    scope.launch { drawerState.close() }
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppVersionText(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    val versionName = packageInfo.versionName
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode.toString()
    } else {
        @Suppress("DEPRECATION") packageInfo.versionCode.toString()
    }

    Box(
        modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "v$versionName ($versionCode)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.alpha(0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SideMenuViewPreview() {
    ShoktukKeyboardTheme { SideMenuView() }
}