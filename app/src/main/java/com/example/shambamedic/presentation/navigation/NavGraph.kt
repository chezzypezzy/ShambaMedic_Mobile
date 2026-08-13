package com.example.shambamedic.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.shambamedic.presentation.auth.AuthScreen
import com.example.shambamedic.presentation.camera.CameraScreen
import com.example.shambamedic.presentation.history.HistoryScreen
import com.example.shambamedic.presentation.home.HomeScreen
import com.example.shambamedic.presentation.notifications.NotificationsScreen
import com.example.shambamedic.presentation.profile.ProfileScreen
import com.example.shambamedic.presentation.results.ResultsScreen

@Composable
fun ShambaMedicNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Auth.route) {
            AuthScreen(navController = navController)
        }

        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.Camera.route,
            arguments = listOf(
                navArgument("cropType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cropType = backStackEntry.arguments?.getString("cropType") ?: "maize"
            CameraScreen(
                navController = navController,
                cropType = cropType
            )
        }

        composable(
            route = Screen.Results.route,
            arguments = listOf(
                navArgument("scanId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
            ResultsScreen(
                navController = navController,
                scanId = scanId
            )
        }

        composable(route = Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(route = Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}
