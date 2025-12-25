package com.natrajtechnology.fitfly.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object ExerciseList : Screen("exercise_list")
    object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_detail/$exerciseId"
    }
    object AddExercise : Screen("add_exercise")
    object EditExercise : Screen("edit_exercise/{exerciseId}") {
        fun createRoute(exerciseId: String) = "edit_exercise/$exerciseId"
    }
    object RoutineList : Screen("routine_list")
    object RoutineDetail : Screen("routine_detail/{routineId}") {
        fun createRoute(routineId: String) = "routine_detail/$routineId"
    }
    object AddRoutine : Screen("add_routine")
    object EditRoutine : Screen("edit_routine/{routineId}") {
        fun createRoute(routineId: String) = "edit_routine/$routineId"
    }
    object Map : Screen("map")
}
