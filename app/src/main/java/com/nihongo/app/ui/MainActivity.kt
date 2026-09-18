package com.nihongo.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nihongo.app.ui.theme.NihongoTheme
import com.nihongo.app.ui.home.HomeScreen
import com.nihongo.app.ui.vocabulary.VocabularyScreen
import com.nihongo.app.ui.study.StudyScreen
import com.nihongo.app.ui.settings.SettingsScreen
import com.nihongo.app.ui.tests.TestsScreen
import com.nihongo.app.ui.tests.TestTakerScreen
import com.nihongo.app.ui.results.ResultsScreen
import com.nihongo.app.ui.results.TestDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NihongoTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Study : Screen("study", "Study", Icons.Default.School)
    object Vocabulary : Screen("vocabulary", "Vocab", Icons.Default.Book)
    object Tests : Screen("tests", "Tests", Icons.Default.Quiz)
    object Results : Screen("results", "Results", Icons.Default.Assessment)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Study,
        Screen.Vocabulary,
        Screen.Tests,
        Screen.Results,
        Screen.Settings
    )

    Scaffold(
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val currentTitle = when {
                currentRoute?.startsWith("test_taker") == true -> "Take Test"
                currentRoute?.startsWith("test_detail") == true -> "Test Review"
                else -> items.find { it.route == currentRoute }?.title ?: "Nihongo"
            }
            TopAppBar(
                title = { Text(currentTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute?.startsWith("test_taker") != true && currentRoute?.startsWith("test_detail") != true) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToStudy = {
                        navController.navigate(Screen.Study.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToTests = {
                        navController.navigate(Screen.Tests.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Study.route) { StudyScreen() }
            composable(Screen.Vocabulary.route) { VocabularyScreen() }
            composable(Screen.Tests.route) {
                TestsScreen(onNavigateToTest = { testId ->
                    navController.navigate("test_taker/$testId")
                })
            }
            composable("test_taker/{testId}") { backStackEntry ->
                val testId = backStackEntry.arguments?.getString("testId")?.toLongOrNull() ?: 0L
                TestTakerScreen(testId = testId, onBack = { navController.popBackStack() })
            }
            composable(Screen.Results.route) {
                ResultsScreen(onNavigateToDetail = { testId ->
                    navController.navigate("test_detail/$testId")
                })
            }
            composable("test_detail/{testId}") { backStackEntry ->
                val testId = backStackEntry.arguments?.getString("testId")?.toLongOrNull() ?: 0L
                TestDetailScreen(testId = testId, onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
