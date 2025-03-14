package com.eosrmg.apps.navigationdrawer.ui.view

import BasicInfo_Screen
import HowToEnable_Screen
import SideMenuHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shoktuk.shoktukkeyboard.project.data.Screens
import com.shoktuk.shoktukkeyboard.project.data.SideMenuItem
import com.shoktuk.shoktukkeyboard.project.screens.testKeyboard.TestKeyboard_Screen
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideMenuView() {
    val drawerState = rememberDrawerState(DrawerValue.Open)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val items = listOf(
        SideMenuItem(Screens.HOW_TO_ENABLE.title, Screens.HOW_TO_ENABLE.systemImageName),
        SideMenuItem(Screens.TEST_KEYBAORD.title, Screens.TEST_KEYBAORD.systemImageName),
        SideMenuItem(Screens.BASIC_INFO.title, Screens.BASIC_INFO.systemImageName)
    )

    val screenTitle = when (selectedItemIndex) {
        0 -> Screens.HOW_TO_ENABLE.title
        1 -> Screens.TEST_KEYBAORD.title
        2 -> Screens.BASIC_INFO.title
        else -> Screens.BASIC_INFO.title
    }

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.75f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .padding(top = 50.dp)
                        .padding(bottom = 25.dp)
                ) {
                    SideMenuHeader(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items.forEachIndexed { index, drawerItems ->
                    NavigationDrawerItem(
                        selected = selectedItemIndex == index, onClick = {
                        selectedItemIndex = index
                        scope.launch { drawerState.close() }
                        val route = when (selectedItemIndex) {
                            0 -> Screens.HOW_TO_ENABLE.title
                            1 -> Screens.TEST_KEYBAORD.title
                            2 -> Screens.BASIC_INFO.title
                            else -> Screens.BASIC_INFO.title
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
                            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = drawerItems.icon, contentDescription = null, modifier = Modifier.padding(10.dp)
                            )
                            Text(text = drawerItems.title)
                        }
                    }, label = { }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    )
                }
            }
        }) {

        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        text = screenTitle, fontSize = 15.sp, color = Color.White
                    )
                }, colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.background), navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(10.dp), onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Rounded.Menu, contentDescription = "Menu", tint = Color.White
                        )
                    }
                })
            }) { innerPadding ->
            // Use the innerPadding provided by the Scaffold so that the content doesn't hide behind the TopAppBar.
            NavHost(
                navController = navController, startDestination = Screens.BASIC_INFO.title, modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screens.HOW_TO_ENABLE.title) { HowToEnable_Screen() }
                composable(Screens.TEST_KEYBAORD.title) { TestKeyboard_Screen() }
                composable(Screens.BASIC_INFO.title) { BasicInfo_Screen() }
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
