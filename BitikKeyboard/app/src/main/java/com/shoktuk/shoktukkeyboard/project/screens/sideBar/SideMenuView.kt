package com.shoktuk.shoktukkeyboard

import BasicInfo_Screen
import HowToEnable_Screen
import Language
import LetterMemorizeScreenCompose
import LocalizationManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shoktuk.bittik.instructions.UseInstruction
import com.shoktuk.bittik.rules.BitikRule1
import com.shoktuk.bittik.rules.BitikRule2
import com.shoktuk.bittik.rules.BitikRule3
import com.shoktuk.bittik.rules.BitikRule4
import com.shoktuk.shoktukkeyboard.project.data.SettingScreens
import com.shoktuk.shoktukkeyboard.project.data.SideMenuItem
import com.shoktuk.shoktukkeyboard.project.screens.settings.CenteredDropdownPopup
import com.shoktuk.shoktukkeyboard.project.screens.settings.SavedStringsScreen
import com.shoktuk.shoktukkeyboard.project.screens.settings.SettingsScreen
import com.shoktuk.shoktukkeyboard.project.screens.testKeyboard.TestKeyboard_Screen
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_BasicInfo
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_SideMenu
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

    val items = Loc_SideMenu.entries.map { SideMenuItem(it, it.systemImage) }
    val mainRoutes = Loc_SideMenu.entries.map { it.titleKey }.toSet()

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Loc_SideMenu.HOW_TO_ENABLE.titleKey
    val isMainScreen = currentRoute in mainRoutes

    var currentLanguage by remember { mutableStateOf(LocalizationManager.currentLanguage) }

    val uriHandler = LocalUriHandler.current
    val rawText = "languageDisclaimer".localized("sideBar", context)

    ModalNavigationDrawer(drawerState = drawerState, gesturesEnabled = isMainScreen, drawerContent = {
        ModalDrawerSheet {
            SideMenuHeader(
                modifier = Modifier
                    .padding(top = 25.dp)
                    .padding(bottom = 10.dp)
            )
            items.forEach { drawerItem ->
                val selected = currentRoute == drawerItem.path.titleKey
                NavigationDrawerItem(
                    selected = selected, onClick = {
                        navigateRoot(scope, drawerState, navController, drawerItem.path.titleKey)
                    }, icon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = drawerItem.icon, contentDescription = null, modifier = Modifier.padding(10.dp)
                            )
                            Text(text = drawerItem.path.titleKey.localized("sideBar", context))
                        }
                    }, label = {}, modifier = Modifier.padding(5.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            val annotated = buildAnnotatedString {
                val parts: List<String> = rawText.split("[link]")
                append(parts.getOrNull(0).orEmpty())

                // Insert link
                withLink(
                    LinkAnnotation.Url(
                        url = "https://t.me/shoktuk_bitik", styles = TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append("Telegram")
                }

                append(parts.getOrNull(1).orEmpty())
            }

            if (LocalizationManager.currentLanguage != Language.KY_L && LocalizationManager.currentLanguage != Language.KY_K) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 15.dp, bottom = 15.dp, end = 15.dp, start = 15.dp)
                ) {
                    Text(
                        text = annotated, fontSize = 16.sp, style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, end = 15.dp, start = 15.dp)
                    )
                    CenteredDropdownPopup(
                        label = "sideBar_language".localized("sideBar", context), options = Language.entries, selected = currentLanguage, onSelect = { alpha ->
                            LocalizationManager.setLanguage(context, newLanguage = alpha)
                            currentLanguage = alpha
                        }, optionLabel = { it.displayName }, modifier = Modifier.fillMaxWidth()
                    )

                    AppVersionText(modifier = Modifier.padding(bottom = 25.dp))
                }

            } else {
                CenteredDropdownPopup(
                    label = "sideBar_language".localized("sideBar", context), options = Language.entries, selected = currentLanguage, onSelect = { alpha ->
                        LocalizationManager.setLanguage(context, newLanguage = alpha)
                        currentLanguage = alpha
                    }, optionLabel = { it.displayName }, modifier = Modifier.fillMaxWidth()
                )

                AppVersionText(modifier = Modifier.padding(bottom = 25.dp))
            }
        }
    }) {
        Scaffold(
            topBar = {
                if (isMainScreen) {
                    TopAppBar(
                        title = {
                            val menuTitle = Loc_SideMenu.entries.firstOrNull { it.titleKey == currentRoute }?.titleKey?.localized("sideBar", context) ?: ""
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
                            val title = Loc_BasicInfo.entries.firstOrNull { it.titleKey == currentRoute }?.titleKey?.localized("basicInfo", context)
                                ?: SettingScreens.entries.firstOrNull { it.id == currentRoute }?.id?.localized("settings", context) ?: ""
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
                navController = navController, startDestination = Loc_SideMenu.HOW_TO_ENABLE.titleKey, modifier = Modifier.padding(innerPadding)
            ) {
                composable(Loc_SideMenu.HOW_TO_ENABLE.titleKey) { HowToEnable_Screen() }
                composable(Loc_SideMenu.TEST_KEYBOARD.titleKey) { TestKeyboard_Screen() }
                composable(Loc_SideMenu.BASIC_INFO.titleKey) { BasicInfo_Screen(navController) }
                composable(Loc_SideMenu.SETTINGS.titleKey) {
                    SettingsScreen(
                        onOpenSavedStrings = { navController.navigate(SettingScreens.SavedStrings.id) })
                }
                composable(Loc_SideMenu.SUPPORT.titleKey) { SupportScreen() }

                composable(Loc_BasicInfo.USING_THE_KEYBOARD.titleKey) { UseInstruction() }
                composable(Loc_BasicInfo.ORIGINAL_BITIK.titleKey) { OriginalTamgasView() }
                composable(Loc_BasicInfo.RULE1.titleKey) { BitikRule1() }
                composable(Loc_BasicInfo.RULE2.titleKey) { BitikRule2() }
                composable(Loc_BasicInfo.RULE3.titleKey) { BitikRule3() }
                composable(Loc_BasicInfo.RULE4.titleKey) { BitikRule4() }
                composable(Loc_BasicInfo.MEMORIZE_TAMGAS.titleKey) { LetterMemorizeScreenCompose() }

                composable(SettingScreens.SavedStrings.id) { SavedStringsScreen() }
            }
        }
    }
}

private fun navigateRoot(
    scope: CoroutineScope, drawerState: DrawerState, navController: NavHostController, route: String
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