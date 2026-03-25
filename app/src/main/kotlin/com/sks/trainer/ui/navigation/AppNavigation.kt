package com.sks.trainer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sks.trainer.ui.screens.HomeScreen
import com.sks.trainer.ui.screens.CategorySelectionScreen
import com.sks.trainer.ui.screens.LearningScreen
import com.sks.trainer.ui.screens.TestScreen
import com.sks.trainer.ui.screens.StatsScreen
import com.sks.trainer.ui.screens.LegalInfoScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CategorySelection : Screen("category_selection/{mode}") {
        fun createRoute(mode: String) = "category_selection/$mode"
    }
    object Learning : Screen("learning/{category}/{bookmarksOnly}") {
        fun createRoute(category: String, bookmarksOnly: Boolean) = "learning/$category/$bookmarksOnly"
    }
    object Test : Screen("test/{category}/{bookmarksOnly}") {
        fun createRoute(category: String, bookmarksOnly: Boolean) = "test/$category/$bookmarksOnly"
    }
    object Stats : Screen("stats")
    object LegalInfo : Screen("legal_info")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLernen = {
                    navController.navigate(Screen.CategorySelection.createRoute("lernen"))
                },
                onNavigateToTest = {
                    navController.navigate(Screen.CategorySelection.createRoute("test"))
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Stats.route)
                },
                onNavigateToLegalInfo = {
                    navController.navigate(Screen.LegalInfo.route)
                }
            )
        }

        composable(
            route = Screen.CategorySelection.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "lernen"
            CategorySelectionScreen(
                mode = mode,
                onCategorySelected = { category, bookmarksOnly ->
                    if (mode == "lernen") {
                        navController.navigate(Screen.Learning.createRoute(category, bookmarksOnly))
                    } else {
                        navController.navigate(Screen.Test.createRoute(category, bookmarksOnly))
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Learning.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("bookmarksOnly") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val bookmarksOnly = backStackEntry.arguments?.getBoolean("bookmarksOnly") ?: false
            LearningScreen(
                category = category,
                bookmarksOnly = bookmarksOnly,
                // Vorher: popBackStack(Screen.Home.route, false) -> Sprang immer direkt auf den Home-Screen
                // Neu: Nur popBackStack() -> Geht exakt einen Schritt zurück (zur Kategorie-Auswahl)
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Test.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("bookmarksOnly") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val bookmarksOnly = backStackEntry.arguments?.getBoolean("bookmarksOnly") ?: false
            TestScreen(
                category = category,
                bookmarksOnly = bookmarksOnly,
                // Vorher: popBackStack(Screen.Home.route, false) -> Sprang immer direkt auf den Home-Screen
                // Neu: Nur popBackStack() -> Geht exakt einen Schritt zurück (zur Kategorie-Auswahl)
                onBack = { navController.popBackStack() },
                // Eine zusätzliche Callback-Option nur für das erfolgreiche *Beenden* des Tests
                onTestFinished = { navController.popBackStack(Screen.Home.route, false) }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        
        composable(Screen.LegalInfo.route) {
            LegalInfoScreen(onBack = { navController.popBackStack() })
        }
    }
}
