package com.example.shambamedic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.shambamedic.presentation.navigation.Screen
import com.example.shambamedic.presentation.navigation.ShambaMedicNavGraph
import com.example.shambamedic.ui.theme.ShambaMedicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShambaMedicTheme {
                val navController = rememberNavController()
                ShambaMedicNavGraph(
                    navController = navController,
                    startDestination = Screen.Auth.route
                )
            }
        }
    }
}
