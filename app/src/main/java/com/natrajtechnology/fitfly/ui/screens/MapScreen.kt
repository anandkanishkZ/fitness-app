package com.natrajtechnology.fitfly.ui.screens

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.natrajtechnology.fitfly.data.model.Exercise
import com.natrajtechnology.fitfly.data.model.GeoTag
import com.natrajtechnology.fitfly.data.model.LocationType
import com.natrajtechnology.fitfly.data.model.WorkoutRoutine

/**
 * Map Screen
 * Shows all geotagged exercises and routines on a map
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    exercises: List<Exercise>,
    routines: List<WorkoutRoutine>,
    onNavigateBack: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onRoutineClick: (String) -> Unit
) {
    val context = LocalContext.current
    
    // Check if Google Maps API key is configured
    val hasValidApiKey = remember {
        try {
            val appInfo: ApplicationInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val bundle: Bundle? = appInfo.metaData
            val apiKey = bundle?.getString("com.google.android.geo.API_KEY")
            !apiKey.isNullOrEmpty() && apiKey != "YOUR_GOOGLE_MAPS_API_KEY_HERE"
        } catch (e: Exception) {
            false
        }
    }
    
    if (!hasValidApiKey) {
        // Show setup instructions instead of map
        MapSetupScreen(onNavigateBack = onNavigateBack)
        return
    }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: LatLng(37.7749, -122.4194), // Default: San Francisco
            12f
        )
    }
    
    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    
    // Get current location
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            12f
                        )
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Locations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!hasLocationPermission) {
                        TextButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Text("Enable Location")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                currentLocation?.let {
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
                                }
                            }
                        ) {
                            Icon(Icons.Default.LocationOn, "My Location")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!hasLocationPermission) {
                // Show permission request UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Location permission is required to view the map",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        Text("Grant Permission")
                    }
                }
            } else {
                // Show Google Map
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = hasLocationPermission
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false
                    )
                ) {
                    // Add markers for exercises
                    exercises.forEach { exercise ->
                        exercise.geoTag?.let { geoTag ->
                            Marker(
                                state = MarkerState(
                                    position = LatLng(geoTag.latitude, geoTag.longitude)
                                ),
                                title = exercise.name,
                                snippet = "${geoTag.locationName} (${geoTag.locationType.name})",
                                onClick = {
                                    onExerciseClick(exercise.id)
                                    true
                                }
                            )
                        }
                    }
                    
                    // Add markers for routines
                    routines.forEach { routine ->
                        routine.geoTag?.let { geoTag ->
                            Marker(
                                state = MarkerState(
                                    position = LatLng(geoTag.latitude, geoTag.longitude)
                                ),
                                title = routine.name,
                                snippet = "${geoTag.locationName} (${geoTag.locationType.name})",
                                onClick = {
                                    onRoutineClick(routine.id)
                                    true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Map Setup Screen
 * Shows when Google Maps API key is not configured
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSetupScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map Setup Required") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Google Maps API Key Required",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "To use the map feature, you need to configure a Google Maps API key.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "✅ Good News:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Google Maps is FREE for testing\n• $200/month free credit\n• Takes only 5 minutes to setup",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Setup Steps:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Visit: console.cloud.google.com\n" +
                                "2. Create a project (or use existing)\n" +
                                "3. Enable 'Maps SDK for Android'\n" +
                                "4. Create an API Key\n" +
                                "5. Add key to AndroidManifest.xml\n" +
                                "6. Rebuild the app",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "See GEOTAGGING_SETUP.md for detailed instructions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "⚠️ The app works perfectly without maps!\nYou can still add location tags to exercises.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
