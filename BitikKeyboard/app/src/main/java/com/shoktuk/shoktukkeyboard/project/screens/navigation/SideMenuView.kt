package com.shoktuk.shoktukkeyboard

import BasicInfo_Screen
import HowToEnable_Screen
import Language
import LetterMemorizeScreenCompose
import LocalizationManager
import OriginalTamgasView
import SideMenuHeader
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
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
import androidx.compose.ui.res.painterResource
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
import com.shoktuk.shoktukkeyboard.project.screens.navigation.BottomNavigationBar
import com.shoktuk.shoktukkeyboard.project.screens.navigation.ExternalLink_Button
import com.shoktuk.shoktukkeyboard.project.screens.navigation.MainTabs
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

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: MainTabs.HOW_TO_ENABLE.route

    val isMainScreen = MainTabs.entries.any { it.route == currentRoute }

    val items = Loc_SideMenu.entries.map { SideMenuItem(it, it.systemImage) }

    var currentLanguage by remember { mutableStateOf(LocalizationManager.currentLanguage) }
    val uriHandler = LocalUriHandler.current
    val rawText = "languageDisclaimer".localized("sideBar", context)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isMainScreen,
        drawerContent = {
            ModalDrawerSheet {
                SideMenuHeader(
                    modifier = Modifier
                        .padding(top = 25.dp)
                        .padding(bottom = 10.dp)
                )

                items.forEach { drawerItem ->
                    val isMainTab = when(drawerItem.path) {
                        Loc_SideMenu.HOW_TO_ENABLE,
                        Loc_SideMenu.TEST_KEYBOARD,
                        Loc_SideMenu.BASIC_INFO,
                        Loc_SideMenu.SETTINGS -> true
                        else -> false
                    }

                    val selected = if (isMainTab) {
                        val tabRoute = when(drawerItem.path) {
                            Loc_SideMenu.HOW_TO_ENABLE -> MainTabs.HOW_TO_ENABLE.route
                            Loc_SideMenu.TEST_KEYBOARD -> MainTabs.TEST_KEYBOARD.route
                            Loc_SideMenu.BASIC_INFO -> MainTabs.BASIC_INFO.route
                            Loc_SideMenu.SETTINGS -> MainTabs.SETTINGS.route
                            else -> ""
                        }
                        currentRoute == tabRoute
                    } else {
                        currentRoute == drawerItem.path.titleKey
                    }

                    NavigationDrawerItem(
                        selected = selected,
                        onClick = {
                            if (isMainTab) {
                                val route = when(drawerItem.path) {
                                    Loc_SideMenu.HOW_TO_ENABLE -> MainTabs.HOW_TO_ENABLE.route
                                    Loc_SideMenu.TEST_KEYBOARD -> MainTabs.TEST_KEYBOARD.route
                                    Loc_SideMenu.BASIC_INFO -> MainTabs.BASIC_INFO.route
                                    Loc_SideMenu.SETTINGS -> MainTabs.SETTINGS.route
                                    else -> MainTabs.HOW_TO_ENABLE.route
                                }
                                scope.launch { drawerState.close() }
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                scope.launch { drawerState.close() }
                                navController.navigate(drawerItem.path.titleKey)
                            }
                        },
                        icon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    imageVector = drawerItem.icon,
                                    contentDescription = null,
                                    modifier = Modifier.padding(10.dp)
                                )
                                Text(
                                    text = drawerItem.path.titleKey.localized("sideBar", context)
                                )
                            }
                        },
                        label = {},
                        modifier = Modifier.padding(5.dp)
                    )
                }
                ExternalLink_Button(
                    label = "Bitish 🚀",
                    description = "Битик үйрөнүү тиркемеси! 🥳",
                    leadingPainter = painterResource(id = R.drawable.bitish_icon),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.shoktuk.learnbitik"))
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.weight(1f))

                if (LocalizationManager.currentLanguage != Language.KY_L && LocalizationManager.currentLanguage != Language.KY_K) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(15.dp)
                    ) {
                        val annotated = buildAnnotatedString {
                            val parts: List<String> = rawText.split("[link]")
                            append(parts.getOrNull(0).orEmpty())

                            withLink(
                                LinkAnnotation.Url(
                                    url = "https://t.me/shoktuk_bitik",
                                    styles = TextLinkStyles(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    )
                                )
                            ) {
                                append("Telegram")
                            }
                            append(parts.getOrNull(1).orEmpty())
                        }

                        Text(
                            text = annotated,
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))

                        CenteredDropdownPopup(
                            label = "sideBar_language".localized("sideBar", context),
                            options = Language.entries,
                            selected = currentLanguage,
                            onSelect = { newLang ->
                                LocalizationManager.setLanguage(context, newLanguage = newLang)
                                currentLanguage = newLang
                            },
                            optionLabel = { it.displayName },
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppVersionText(modifier = Modifier.padding(bottom = 25.dp))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        CenteredDropdownPopup(
                            label = "sideBar_language".localized("sideBar", context),
                            options = Language.entries,
                            selected = currentLanguage,
                            onSelect = { newLang ->
                                LocalizationManager.setLanguage(context, newLanguage = newLang)
                                currentLanguage = newLang
                            },
                            optionLabel = { it.displayName },
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppVersionText(modifier = Modifier.padding(bottom = 25.dp))
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isMainScreen) {
                    TopAppBar(
                        title = {
                            val title = when(MainTabs.fromRoute(currentRoute)) {
                                MainTabs.HOW_TO_ENABLE -> Loc_SideMenu.HOW_TO_ENABLE.titleKey.localized("sideBar", context)
                                MainTabs.TEST_KEYBOARD -> Loc_SideMenu.TEST_KEYBOARD.titleKey.localized("sideBar", context)
                                MainTabs.BASIC_INFO -> Loc_SideMenu.BASIC_INFO.titleKey.localized("sideBar", context)
                                MainTabs.SETTINGS -> Loc_SideMenu.SETTINGS.titleKey.localized("sideBar", context)
                            }
                            Text(text = title, fontSize = 15.sp)
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.background)
                    )
                } else {
                    TopAppBar(
                        title = {
                            val title = when {
                                Loc_BasicInfo.entries.any { it.titleKey == currentRoute } -> {
                                    Loc_BasicInfo.entries.first { it.titleKey == currentRoute }
                                        .titleKey.localized("basicInfo", context)
                                }
                                SettingScreens.entries.any { it.id == currentRoute } -> {
                                    SettingScreens.entries.first { it.id == currentRoute }
                                        .id.localized("settings", context)
                                }
                                else -> ""
                            }
                            Text(text = title, fontSize = 15.sp)
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.background)
                    )
                }
            },
            bottomBar = {
                if (isMainScreen) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigationItemSelected = { tab ->
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MainTabs.HOW_TO_ENABLE.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(MainTabs.HOW_TO_ENABLE.route) {
                    HowToEnable_Screen()
                }
                composable(MainTabs.TEST_KEYBOARD.route) {
                    TestKeyboard_Screen()
                }
                composable(MainTabs.BASIC_INFO.route) {
                    BasicInfo_Screen(
                        navController = navController
                    )
                }
                composable(MainTabs.SETTINGS.route) {
                    SettingsScreen(
                        onOpenSavedStrings = {
                            navController.navigate(SettingScreens.SavedStrings.id)
                        }
                    )
                }

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

    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: Exception) {
        null
    }

    val versionName = packageInfo?.versionName ?: "1.0.0"
    val versionCode = packageInfo?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            it.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION") it.versionCode.toString()
        }
    } ?: "1"

    Box(
        modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "v$versionName ($versionCode)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.alpha(0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SideMenuViewPreview() {
    ShoktukKeyboardTheme { SideMenuView() }
}
