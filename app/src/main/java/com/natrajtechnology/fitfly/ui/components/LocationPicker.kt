package com.natrajtechnology.fitfly.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.natrajtechnology.fitfly.data.model.GeoTag
import com.natrajtechnology.fitfly.data.model.LocationType

/**
 * Location Picker Component
 * Allows users to add location tags to exercises and routines
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPicker(
    currentGeoTag: GeoTag?,
    onGeoTagChange: (GeoTag?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var locationName by remember { mutableStateOf(currentGeoTag?.locationName ?: "") }
    var selectedType by remember { mutableStateOf(currentGeoTag?.locationType ?: LocationType.GYM) }
    var address by remember { mutableStateOf(currentGeoTag?.address ?: "") }
    var currentLat by remember { mutableStateOf(currentGeoTag?.latitude ?: 0.0) }
    var currentLng by remember { mutableStateOf(currentGeoTag?.longitude ?: 0.0) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (hasLocationPermission) {
            // Get current location after permission granted
            isLoadingLocation = true
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLat = it.latitude
                        currentLng = it.longitude
                    }
                    isLoadingLocation = false
                }
            } catch (e: SecurityException) {
                isLoadingLocation = false
                e.printStackTrace()
            }
        }
    }
    
    Column(modifier = modifier) {
        // Current location display
        if (currentGeoTag != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentGeoTag.locationName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = currentGeoTag.locationType.name.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { onGeoTagChange(null) }) {
                        Text("Remove")
                    }
                }
            }
        } else {
            // Add location button
            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, "Add Location")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Location Tag")
            }
        }
    }
    
    // Location picker dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Location Tag") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Location name
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Location Name") },
                        placeholder = { Text("e.g., Gold's Gym") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Location type dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedType.name.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Location Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            LocationType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name.replace("_", " ")) },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Address
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address (Optional)") },
                        placeholder = { Text("123 Main St") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Get current location button
                    Button(
                        onClick = {
                            if (!hasLocationPermission) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                isLoadingLocation = true
                                try {
                                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            currentLat = it.latitude
                                            currentLng = it.longitude
                                        }
                                        isLoadingLocation = false
                                    }
                                } catch (e: SecurityException) {
                                    isLoadingLocation = false
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoadingLocation
                    ) {
                        if (isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.LocationOn, "Get Location")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Current Location")
                        }
                    }
                    
                    if (currentLat != 0.0 && currentLng != 0.0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lat: %.6f, Lng: %.6f".format(currentLat, currentLng),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (locationName.isNotBlank() && currentLat != 0.0 && currentLng != 0.0) {
                            onGeoTagChange(
                                GeoTag(
                                    latitude = currentLat,
                                    longitude = currentLng,
                                    locationName = locationName,
                                    locationType = selectedType,
                                    address = address
                                )
                            )
                            showDialog = false
                        }
                    },
                    enabled = locationName.isNotBlank() && currentLat != 0.0 && currentLng != 0.0
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
