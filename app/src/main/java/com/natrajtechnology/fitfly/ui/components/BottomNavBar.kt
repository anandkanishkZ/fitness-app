package com.natrajtechnology.fitfly.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.natrajtechnology.fitfly.navigation.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val showAddModal = remember { mutableStateOf(false) }

    // Only show the bottom bar on main app screens
    val visibleRoutes = setOf(
        Screen.Dashboard.route,
        Screen.Profile.route,
        Screen.ExerciseList.route,
        Screen.RoutineList.route,
        Screen.AddExercise.route,
        Screen.AddRoutine.route
    )

    if (!visibleRoutes.any { currentRoute.startsWith(it) }) return

    // Modal Dialog for choosing Exercise or Routine
    if (showAddModal.value) {
        AlertDialog(
            onDismissRequest = { showAddModal.value = false },
            title = { Text("What would you like to add?") },
            text = { Text("Choose whether to add a new Exercise or a new Routine.") },
            confirmButton = {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            navController.navigate(Screen.AddExercise.route)
                            showAddModal.value = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Add Exercise")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            navController.navigate(Screen.AddRoutine.route)
                            showAddModal.value = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Add Routine")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddModal.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp)),
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        NavigationBarItem(
            selected = currentRoute.startsWith(Screen.Dashboard.route),
            onClick = { navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = false }
                launchSingleTop = true
            } },
            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Dashboard") },
            label = { Text("Home") },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                indicatorColor = Color.White.copy(alpha = 0.3f),
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            )
        )

        // Workouts Menu - Dedicated Routines View
        NavigationBarItem(
            selected = currentRoute.startsWith(Screen.RoutineList.route),
            onClick = { navController.navigate(Screen.RoutineList.route) {
                popUpTo(Screen.RoutineList.route) { inclusive = false }
                launchSingleTop = true
            } },
            icon = { Icon(imageVector = Icons.Filled.FavoriteBorder, contentDescription = "Workouts") },
            label = { Text("Workouts") },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                indicatorColor = Color.White.copy(alpha = 0.3f),
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            )
        )

        // Add Button - Opens Modal (Center)
        NavigationBarItem(
            selected = false,
            onClick = { showAddModal.value = true },
            icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = "Add") },
            label = { Text("Add") },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                indicatorColor = Color.White.copy(alpha = 0.3f),
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            )
        )

        NavigationBarItem(
            selected = currentRoute.startsWith(Screen.Profile.route),
            onClick = { navController.navigate(Screen.Profile.route) {
                launchSingleTop = true
            } },
            icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                indicatorColor = Color.White.copy(alpha = 0.3f),
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            )
        )
    }
}
