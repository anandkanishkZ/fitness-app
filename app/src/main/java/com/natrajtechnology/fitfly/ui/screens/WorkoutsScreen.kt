package com.natrajtechnology.fitfly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.natrajtechnology.fitfly.data.model.WorkoutRoutine
import com.natrajtechnology.fitfly.ui.viewmodel.RoutineViewModel
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    routineViewModel: RoutineViewModel,
    onRoutineClick: (String) -> Unit,
    onAddRoutineClick: () -> Unit
) {
    val routines by routineViewModel.routines.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workouts", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = onAddRoutineClick) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Workout",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Analytics Header Section
            item {
                AnalyticsDashboard(routines = routines)
            }

            // Active Workouts Section
            item {
                WorkoutSectionHeader(
                    title = "Active Workouts",
                    count = routines.count { !it.isCompleted }
                )
            }

            val activeRoutines = routines.filter { !it.isCompleted }
            if (activeRoutines.isEmpty()) {
                item {
                    EmptyStateCard("No active workouts. Create one to get started!")
                }
            } else {
                items(activeRoutines) { routine ->
                    WorkoutCard(
                        routine = routine,
                        onClick = { onRoutineClick(routine.id) }
                    )
                }
            }

            // Completed Workouts Section
            item {
                WorkoutSectionHeader(
                    title = "Completed Workouts",
                    count = routines.count { it.isCompleted }
                )
            }

            val completedRoutines = routines.filter { it.isCompleted }
            if (completedRoutines.isEmpty()) {
                item {
                    EmptyStateCard("No completed workouts yet. Keep pushing!")
                }
            } else {
                items(completedRoutines) { routine ->
                    WorkoutCard(
                        routine = routine,
                        onClick = { onRoutineClick(routine.id) },
                        isCompleted = true
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AnalyticsDashboard(routines: List<WorkoutRoutine>) {
    val totalWorkouts = routines.size
    val completedWorkouts = routines.count { it.isCompleted }
    val completionRate = if (totalWorkouts > 0) (completedWorkouts * 100) / totalWorkouts else 0
    val averageExercisesPerRoutine = if (totalWorkouts > 0) {
        routines.map { it.exerciseIds.size }.average().toInt()
    } else {
        0
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Stats Row - 2 columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsCard(
                icon = Icons.Filled.FavoriteBorder,
                title = "Total Workouts",
                value = totalWorkouts.toString(),
                backgroundColor = Color(0xFF84E4B6).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            )

            AnalyticsCard(
                icon = Icons.Filled.CheckCircle,
                title = "Completed",
                value = completedWorkouts.toString(),
                backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            )
        }

        // Secondary Stats Row - 2 columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsCard(
                icon = Icons.Filled.CheckCircle,
                title = "Completion Rate",
                value = "$completionRate%",
                backgroundColor = Color(0xFF2196F3).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            )

            AnalyticsCard(
                icon = Icons.Filled.FavoriteBorder,
                title = "Avg Exercises",
                value = averageExercisesPerRoutine.toString(),
                backgroundColor = Color(0xFFFF9800).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            )
        }

        // Progress Bar for Completion Rate
        CompletionProgressBar(completionRate)
    }
}

@Composable
fun AnalyticsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CompletionProgressBar(completionRate: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Progress",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$completionRate%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { completionRate / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun WorkoutSectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun WorkoutCard(
    routine: WorkoutRoutine,
    onClick: () -> Unit,
    isCompleted: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isCompleted) {
                    Color.White.copy(alpha = 0.08f)
                } else {
                    Color.White.copy(alpha = 0.12f)
                }
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = if (isCompleted) {
                            Modifier
                        } else {
                            Modifier
                        }
                    )

                    if (routine.description.isNotEmpty()) {
                        Text(
                            text = routine.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                // Status badge
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp)),
                    color = if (isCompleted) {
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = if (isCompleted) "Completed" else "Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Exercises info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(
                    icon = Icons.Filled.FavoriteBorder,
                    label = "Exercises",
                    value = routine.exerciseIds.size.toString()
                )

                if (routine.requiredEquipment.isNotEmpty()) {
                    InfoChip(
                        icon = Icons.Filled.Build,
                        label = "Equipment",
                        value = routine.requiredEquipment.size.toString()
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
