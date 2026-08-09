package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.Routes
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DistributionScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TeachersScreen
import com.example.ui.screens.WelcomeScreen

@Composable
fun MainScreen(
    subjectViewModel: SubjectViewModel,
    teacherViewModel: TeacherViewModel,
    distributionViewModel: DistributionViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val hasSeenWelcome = sharedPrefs.getBoolean("has_seen_welcome", false)

    val items = listOf(
        Triple(Routes.DASHBOARD, "الرئيسية", Icons.Filled.Dashboard),
        Triple(Routes.SUBJECTS, "المواد", Icons.AutoMirrored.Filled.List),
        Triple(Routes.TEACHERS, "المعلمون", Icons.Filled.Group),
        Triple(Routes.DISTRIBUTION, "التوزيع", Icons.Filled.Assessment),
        Triple(Routes.SETTINGS, "الإعدادات", Icons.Filled.Settings)
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            if (currentDestination?.route != Routes.WELCOME) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                navController.navigate(route) {
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
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (hasSeenWelcome) Routes.DASHBOARD else Routes.WELCOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(onStartClicked = {
                    sharedPrefs.edit().putBoolean("has_seen_welcome", true).apply()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                })
            }
            composable(Routes.DASHBOARD) { DashboardScreen(distributionViewModel, subjectViewModel, teacherViewModel, settingsViewModel) }
            composable(Routes.SUBJECTS) { SubjectsScreen(subjectViewModel) }
            composable(Routes.TEACHERS) { TeachersScreen(teacherViewModel, subjectViewModel) }
            composable(Routes.DISTRIBUTION) { DistributionScreen(distributionViewModel, subjectViewModel, settingsViewModel, teacherViewModel) }
            composable(Routes.SETTINGS) { SettingsScreen(settingsViewModel) }
        }
    }
}
