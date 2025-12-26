package com.natrajtechnology.fitfly.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.natrajtechnology.fitfly.ui.screens.*
import com.natrajtechnology.fitfly.ui.viewmodel.AuthViewModel
import com.natrajtechnology.fitfly.ui.viewmodel.ExerciseViewModel
import com.natrajtechnology.fitfly.ui.viewmodel.RoutineViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onPickImage: () -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val startDestination = if (currentUser != null) {
        Screen.Dashboard.route
    } else {
        Screen.Home.route
    }
    
    // Shared ViewModels across navigation graph
    val exerciseViewModel: ExerciseViewModel = viewModel()
    val routineViewModel: RoutineViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Sign Up Screen
        composable(Screen.SignUp.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Dashboard Screen
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                authViewModel = authViewModel,
                exerciseViewModel = exerciseViewModel,
                routineViewModel = routineViewModel,
                onLogout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onAddExercise = { navController.navigate(Screen.AddExercise.route) },
                onAddRoutine = { navController.navigate(Screen.AddRoutine.route) },
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.EditExercise.createRoute(exerciseId))
                },
                onRoutineClick = { routineId ->
                    navController.navigate(Screen.RoutineDetail.createRoute(routineId))
                },
                onMapClick = { navController.navigate(Screen.Map.route) },
                onViewAllExercises = { navController.navigate(Screen.ExerciseList.route) },
                onViewAllRoutines = { navController.navigate(Screen.RoutineList.route) }
            )
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onPickImage = onPickImage
            )
        }

        // Add Exercise Screen
        composable(Screen.AddExercise.route) {
            AddEditExerciseScreen(
                exerciseViewModel = exerciseViewModel,
                exerciseId = null,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // Exercise List Screen
        composable(Screen.ExerciseList.route) {
            ExerciseListScreen(
                exerciseViewModel = exerciseViewModel,
                onBackClick = { navController.popBackStack() },
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.EditExercise.createRoute(exerciseId))
                },
                onAddExerciseClick = {
                    navController.navigate(Screen.AddExercise.route)
                }
            )
        }

        // Edit Exercise Screen
        composable(
            route = Screen.EditExercise.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")
            
            AddEditExerciseScreen(
                exerciseViewModel = exerciseViewModel,
                exerciseId = exerciseId,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // Add Routine Screen
        composable(Screen.AddRoutine.route) {
            AddEditRoutineScreen(
                routineViewModel = routineViewModel,
                exerciseViewModel = exerciseViewModel,
                routineId = null,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // Routine Detail Screen
        composable(
            route = Screen.RoutineDetail.route,
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId")
            
            if (routineId != null) {
                RoutineDetailScreen(
                    routineViewModel = routineViewModel,
                    exerciseViewModel = exerciseViewModel,
                    routineId = routineId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate(Screen.EditRoutine.createRoute(routineId)) }
                )
            }
        }

        // Edit Routine Screen
        composable(
            route = Screen.EditRoutine.route,
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId")
            
            AddEditRoutineScreen(
                routineViewModel = routineViewModel,
                exerciseViewModel = exerciseViewModel,
                routineId = routineId,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // Workouts Screen (Routine List with Analytics)
        composable(Screen.RoutineList.route) {
            WorkoutsScreen(
                routineViewModel = routineViewModel,
                onRoutineClick = { routineId ->
                    navController.navigate(Screen.RoutineDetail.createRoute(routineId))
                },
                onAddRoutineClick = {
                    navController.navigate(Screen.AddRoutine.route)
                }
            )
        }
        
        // Map Screen
        composable(Screen.Map.route) {
            val exercises by exerciseViewModel.exercises.collectAsState()
            val routines by routineViewModel.routines.collectAsState()
            
            MapScreen(
                exercises = exercises,
                routines = routines,
                onNavigateBack = { navController.popBackStack() },
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.EditExercise.createRoute(exerciseId))
                },
                onRoutineClick = { routineId ->
                    navController.navigate(Screen.RoutineDetail.createRoute(routineId))
                }
            )
        }
    }
}
