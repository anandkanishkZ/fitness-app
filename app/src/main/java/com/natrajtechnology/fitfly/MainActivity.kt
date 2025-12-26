package com.natrajtechnology.fitfly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.natrajtechnology.fitfly.navigation.AppNavigation
import com.natrajtechnology.fitfly.ui.theme.FitflyTheme
import com.natrajtechnology.fitfly.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitflyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    
                    // Scaffold with modern bottom button navigation
                    Scaffold(
                        bottomBar = { com.natrajtechnology.fitfly.ui.components.BottomNavBar(navController) }
                    ) { padding ->
                        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
                            AppNavigation(
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}