package com.natrajtechnology.fitfly.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.natrajtechnology.fitfly.data.model.Exercise
import com.natrajtechnology.fitfly.data.model.WorkoutRoutine
import com.natrajtechnology.fitfly.ui.viewmodel.AuthViewModel
import com.natrajtechnology.fitfly.ui.viewmodel.ExerciseViewModel
import com.natrajtechnology.fitfly.ui.viewmodel.RoutineViewModel
import com.natrajtechnology.fitfly.ui.viewmodel.UiState
import kotlin.math.sqrt

/**
 * Dashboard Screen
 * Main screen showing exercises and routines with gesture controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    exerciseViewModel: ExerciseViewModel,
    routineViewModel: RoutineViewModel,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onAddExercise: () -> Unit,
    onAddRoutine: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onRoutineClick: (String) -> Unit,
    onMapClick: () -> Unit
) {
    val exercises by exerciseViewModel.exercises.collectAsState()
    val routines by routineViewModel.routines.collectAsState()
    val exerciseUiState by exerciseViewModel.uiState.collectAsState()
    val routineUiState by routineViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Debug logging
    LaunchedEffect(exercises.size) {
        android.util.Log.d("DashboardScreen", "Exercises count in UI: ${exercises.size}")
        exercises.forEach { exercise ->
            android.util.Log.d("DashboardScreen", "Exercise: ${exercise.name}, userId: ${exercise.userId}")
        }
    }
    
    LaunchedEffect(routines.size) {
        android.util.Log.d("DashboardScreen", "Routines count in UI: ${routines.size}")
    }
    
    var showMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showShakeResetDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    
    val context = LocalContext.current
    
    // Shake detection
    val shakeDetector = remember(context) {
        ShakeDetector(
            context = context,
            onShake = {
                showShakeResetDialog = true
            }
        )
    }

    DisposableEffect(shakeDetector) {
        shakeDetector.start()
        onDispose { shakeDetector.stop() }
    }

    LaunchedEffect(exerciseUiState) {
        when (exerciseUiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar((exerciseUiState as UiState.Success).message)
                exerciseViewModel.clearUiState()
            }

            is UiState.Error -> {
                snackbarHostState.showSnackbar((exerciseUiState as UiState.Error).message)
                exerciseViewModel.clearUiState()
            }

            else -> Unit
        }
    }

    LaunchedEffect(routineUiState) {
        when (routineUiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar((routineUiState as UiState.Success).message)
                routineViewModel.clearUiState()
            }

            is UiState.Error -> {
                snackbarHostState.showSnackbar((routineUiState as UiState.Error).message)
                routineViewModel.clearUiState()
            }

            else -> Unit
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text("Delete ${itemToDelete!!.first}") },
            text = { Text("Are you sure you want to delete this ${itemToDelete!!.first.lowercase()}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (itemToDelete!!.first == "Exercise") {
                            exerciseViewModel.deleteExercise(itemToDelete!!.second)
                        } else {
                            routineViewModel.deleteRoutine(itemToDelete!!.second)
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Shake reset dialog
    if (showShakeResetDialog) {
        AlertDialog(
            onDismissRequest = { showShakeResetDialog = false },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
            title = { Text("Reset Workout Checklist") },
            text = { Text("This will mark all exercises and routines as incomplete. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Reset all completed items to incomplete
                        exercises.filter { it.isCompleted }.forEach {
                            exerciseViewModel.toggleExerciseCompletion(it.id, it.isCompleted)
                        }
                        routines.filter { it.isCompleted }.forEach {
                            routineViewModel.toggleRoutineCompletion(it.id, it.isCompleted)
                        }
                        showShakeResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShakeResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("FitLife")
                        Text(
                            text = "Welcome, ${currentUser?.displayName ?: "User"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onMapClick) {
                        Icon(Icons.Default.LocationOn, contentDescription = "View Map")
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                showMenu = false
                                onProfileClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                authViewModel.signOut()
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ExitToApp, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTab == 0) {
                    SmallFloatingActionButton(
                        onClick = onAddExercise,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                    }
                } else {
                    SmallFloatingActionButton(
                        onClick = onAddRoutine,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Routine")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Exercises") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Routines") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
            }
            
            // Gesture hint
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Swipe left to delete • Swipe right to complete • Shake to reset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Content
            when (selectedTab) {
                0 -> ExerciseList(
                    exercises = exercises,
                    onExerciseClick = onExerciseClick,
                    onToggleComplete = { id, isCompleted ->
                        exerciseViewModel.toggleExerciseCompletion(id, isCompleted)
                    },
                    onDelete = { id ->
                        itemToDelete = Pair("Exercise", id)
                        showDeleteDialog = true
                    }
                )
                1 -> RoutineList(
                    routines = routines,
                    onRoutineClick = onRoutineClick,
                    onToggleComplete = { id, isCompleted ->
                        routineViewModel.toggleRoutineCompletion(id, isCompleted)
                    },
                    onDelete = { id ->
                        itemToDelete = Pair("Routine", id)
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    onExerciseClick: (String) -> Unit,
    onToggleComplete: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    if (exercises.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No exercises yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Tap + to add your first exercise",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exercises, key = { it.id }) { exercise ->
                ExerciseItem(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise.id) },
                    onToggleComplete = { onToggleComplete(exercise.id, exercise.isCompleted) },
                    onDelete = { onDelete(exercise.id) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExerciseItem(
    exercise: Exercise,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    // Keep this relatively low so swipe-right is easy to trigger.
    // (Swiping right starting near the very left edge may conflict with Android's Back gesture.)
    val swipeThreshold = 80f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > swipeThreshold) {
                            // Swipe right - complete
                            onToggleComplete()
                        } else if (offsetX < -swipeThreshold) {
                            // Swipe left - delete
                            onDelete()
                        }
                        offsetX = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDelete
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (exercise.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onToggleComplete()
                    }
            ) {
                Checkbox(
                    checked = exercise.isCompleted,
                    onCheckedChange = null
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (exercise.isCompleted) TextDecoration.LineThrough else null
                )
                Text(
                    text = "${exercise.sets} sets × ${exercise.reps} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (exercise.requiredEquipment.isNotEmpty()) {
                    Text(
                        text = "Equipment: ${exercise.requiredEquipment.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoutineList(
    routines: List<WorkoutRoutine>,
    onRoutineClick: (String) -> Unit,
    onToggleComplete: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    if (routines.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No routines yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Tap + to create your first routine",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(routines, key = { it.id }) { routine ->
                RoutineItem(
                    routine = routine,
                    onClick = { onRoutineClick(routine.id) },
                    onToggleComplete = { onToggleComplete(routine.id, routine.isCompleted) },
                    onDelete = { onDelete(routine.id) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoutineItem(
    routine: WorkoutRoutine,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val swipeThreshold = 80f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > swipeThreshold) {
                            // Swipe right - complete
                            onToggleComplete()
                        } else if (offsetX < -swipeThreshold) {
                            // Swipe left - delete
                            onDelete()
                        }
                        offsetX = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDelete
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (routine.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onToggleComplete()
                    }
            ) {
                Checkbox(
                    checked = routine.isCompleted,
                    onCheckedChange = null
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (routine.isCompleted) TextDecoration.LineThrough else null
                )
                if (routine.description.isNotEmpty()) {
                    Text(
                        text = routine.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (routine.requiredEquipment.isNotEmpty()) {
                    Text(
                        text = "Equipment: ${routine.requiredEquipment.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onClick) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "View Details")
            }
        }
    }
}

// Shake Detector Class
class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var lastShakeTime: Long = 0
    private val shakeThreshold = 15f
    private val shakeCooldown = 2000L // 2 seconds
    
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }
    
    fun stop() {
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            
            val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
            
            if (acceleration > shakeThreshold) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime > shakeCooldown) {
                    lastShakeTime = currentTime
                    onShake()
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}
