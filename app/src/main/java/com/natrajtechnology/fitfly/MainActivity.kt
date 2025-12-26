package com.natrajtechnology.fitfly

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.natrajtechnology.fitfly.ui.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Upload image to Cloudinary via ViewModel
            authViewModel.updateProfilePhoto(it)
        }
    }

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
                    authViewModel = viewModel(factory = AuthViewModelFactory(this@MainActivity))
                    
                    // Scaffold with modern bottom button navigation
                    Scaffold(
                        bottomBar = { com.natrajtechnology.fitfly.ui.components.BottomNavBar(navController) }
                    ) { padding ->
                        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
                            AppNavigation(
                                navController = navController,
                                authViewModel = authViewModel,
                                onPickImage = { pickImageLauncher.launch("image/*") }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Handle image picker from old API
    @Deprecated("Use activity result launcher instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            imageUri?.let {
                authViewModel.updateProfilePhoto(it)
            }
        }
    }
}