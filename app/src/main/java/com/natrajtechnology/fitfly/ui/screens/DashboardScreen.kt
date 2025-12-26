package com.natrajtechnology.fitfly.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onMapClick: () -> Unit,
    onViewAllExercises: () -> Unit = {},
    onViewAllRoutines: () -> Unit = {}
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
            icon = { Icon(imageVector = Icons.Default.Refresh, contentDescription = null) },
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
            // Modern Top Bar with Gradient
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hero header card with gradient and quick stats
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(200.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "FitFly",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = "Hello, ${currentUser?.displayName ?: "User"}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        // small quick stat chips - exercise done
                                        ElevatedCard(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier
                                                .height(44.dp)
                                                .weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(imageVector = Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                Text("${exercises.count { it.isCompleted }} done", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                            }
                                        }

                                        ElevatedCard(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier
                                                .height(44.dp)
                                                .weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(imageVector = Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                                Text("${routines.size} routines", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stats Overview Cards (kept as horizontal list below hero)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        item {
                            StatsCard(
                                icon = Icons.Filled.Favorite,
                                title = "Exercises",
                                value = exercises.size.toString(),
                                subtitle = "${exercises.count { it.isCompleted }} completed",
                                color = MaterialTheme.colorScheme.tertiary,
                                onClick = { onViewAllExercises() }
                            )
                        }
                        item {
                            StatsCard(
                                icon = Icons.Filled.DateRange,
                                title = "Routines",
                                value = routines.size.toString(),
                                subtitle = "${routines.count { it.isCompleted }} completed",
                                color = MaterialTheme.colorScheme.secondary,
                                onClick = { onViewAllRoutines() }
                            )
                        }
                        item {
                            val totalProgress = if ((exercises.size + routines.size) > 0) {
                                ((exercises.count { it.isCompleted } + routines.count { it.isCompleted }) * 100) /
                                        (exercises.size + routines.size)
                            } else 0
                            StatsCard(
                                icon = Icons.Filled.Star,
                                title = "Progress",
                                value = "$totalProgress%",
                                subtitle = "Overall completion",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { /* Progress card click handler */ }
                            )
                        }
                    }
                    }
                }
            }
        },
        floatingActionButton = {
            // Enhanced FAB with animation
            val scale by animateFloatAsState(
                targetValue = if (selectedTab == 0) 1f else 1f,
                animationSpec = tween(durationMillis = 200)
            )
            
            FloatingActionButton(
                onClick = if (selectedTab == 0) onAddExercise else onAddRoutine,
                modifier = Modifier
                    .scale(scale)
                    .size(64.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (selectedTab == 0) "Add Exercise" else "Add Routine",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Modern Tab Row with Indicator
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        height = 4.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = if (selectedTab == 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Exercises",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = if (selectedTab == 1) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Routines",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Enhanced Gesture Hint Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Swipe left to delete • Swipe right to complete • Shake to reset",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
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

/**
 * Modern Stats Card Component
 */
@Composable
fun StatsCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.5f))
                )
            }
            
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        "No exercises yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap the + button to add your first exercise and start your fitness journey!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
    val swipeThreshold = 80f
    
    // Animate background color based on completion status
    val backgroundColor by animateColorAsState(
        targetValue = if (exercise.isCompleted)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300)
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > swipeThreshold) {
                            onToggleComplete()
                        } else if (offsetX < -swipeThreshold) {
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (exercise.isCompleted) 1.dp else 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox with modern styling
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (exercise.isCompleted)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onToggleComplete()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (exercise.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Not completed",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (exercise.isCompleted) TextDecoration.LineThrough else null,
                    color = if (exercise.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sets and Reps badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${exercise.sets} × ${exercise.reps}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                if (exercise.requiredEquipment.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = exercise.requiredEquipment.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Edit button with icon
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
            ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        "No routines yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap the + button to create your first workout routine and organize your exercises!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
    
    // Animate background color based on completion status
    val backgroundColor by animateColorAsState(
        targetValue = if (routine.isCompleted)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300)
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > swipeThreshold) {
                            onToggleComplete()
                        } else if (offsetX < -swipeThreshold) {
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (routine.isCompleted) 1.dp else 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox with modern styling
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (routine.isCompleted)
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onToggleComplete()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (routine.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Not completed",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (routine.isCompleted) TextDecoration.LineThrough else null,
                    color = if (routine.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (routine.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = routine.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
                
                if (routine.requiredEquipment.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = routine.requiredEquipment.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // View details button with icon
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
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
