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

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CategorySelection : Screen("category_selection/{mode}") {
        fun createRoute(mode: String) = "category_selection/$mode"
    }
    object Learning : Screen("learning/{category}") {
        fun createRoute(category: String) = "learning/$category"
    }
    object Test : Screen("test/{category}") {
        fun createRoute(category: String) = "test/$category"
    }
    object Stats : Screen("stats")
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
                onCategorySelected = { category ->
                    if (mode == "lernen") {
                        navController.navigate(Screen.Learning.createRoute(category))
                    } else {
                        navController.navigate(Screen.Test.createRoute(category))
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Learning.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            LearningScreen(
                category = category,
                onBack = { navController.popBackStack(Screen.Home.route, false) }
            )
        }

        composable(
            route = Screen.Test.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            TestScreen(
                category = category,
                onBack = { navController.popBackStack(Screen.Home.route, false) }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
