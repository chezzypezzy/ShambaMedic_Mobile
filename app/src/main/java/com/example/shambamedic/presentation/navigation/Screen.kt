package com.example.shambamedic.presentation.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Camera : Screen("camera/{cropType}") {
        fun createRoute(cropType: String) = "camera/$cropType"
    }
    object Results : Screen("results/{scanId}") {
        fun createRoute(scanId: String) = "results/$scanId"
    }
    object History : Screen("history")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
}
