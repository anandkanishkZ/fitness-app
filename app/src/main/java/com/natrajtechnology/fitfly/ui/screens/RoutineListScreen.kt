package com.natrajtechnology.fitfly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.natrajtechnology.fitfly.data.model.WorkoutRoutine
import com.natrajtechnology.fitfly.ui.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
    routineViewModel: RoutineViewModel,
    onBackClick: () -> Unit,
    onRoutineClick: (String) -> Unit,
    onAddRoutineClick: () -> Unit
) {
    val routines by routineViewModel.routines.collectAsState()
    val searchQuery = remember { mutableStateOf("") }
    
    val filteredRoutines = routines.filter { routine ->
        routine.name.contains(searchQuery.value, ignoreCase = true) ||
        routine.description.contains(searchQuery.value, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Routines", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = onAddRoutineClick) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Routine",
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
            // Search Bar
            item {
                SearchBar(
                    query = searchQuery.value,
                    onQueryChange = { searchQuery.value = it },
                    placeholder = "Search routines..."
                )
            }

            // Summary Stats
            item {
                RoutineStats(
                    total = routines.size,
                    completed = routines.count { it.isCompleted },
                    totalExercises = routines.sumOf { it.exerciseIds.size }
                )
            }

            // Empty State
            if (filteredRoutines.isEmpty()) {
                item {
                    EmptyRoutineState(
                        message = if (searchQuery.value.isEmpty())
                            "No routines yet. Create one to get started!"
                        else
                            "No routines found for '${searchQuery.value}'"
                    )
                }
            } else {
                items(filteredRoutines) { routine ->
                    RoutineListItem(
                        routine = routine,
                        onClick = { onRoutineClick(routine.id) }
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
fun RoutineStats(total: Int, completed: Int, totalExercises: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(
            label = "Total",
            value = total.toString(),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )

        StatBox(
            label = "Completed",
            value = completed.toString(),
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )

        StatBox(
            label = "Exercises",
            value = totalExercises.toString(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun RoutineListItem(
    routine: WorkoutRoutine,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        color = MaterialTheme.colorScheme.onBackground
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

                // Completion badge
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                    color = if (routine.isCompleted) {
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (routine.isCompleted) "✓ Done" else "Active",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (routine.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Info Row with Exercise Count and Equipment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChipRoutine(
                    icon = "🏋️",
                    label = "Exercises",
                    value = routine.exerciseIds.size.toString()
                )

                if (routine.requiredEquipment.isNotEmpty()) {
                    InfoChipRoutine(
                        icon = "🔧",
                        label = "Equipment",
                        value = routine.requiredEquipment.size.toString()
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChipRoutine(
    icon: String,
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyRoutineState(message: String) {
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
            Text(
                text = "🎯",
                style = MaterialTheme.typography.displaySmall
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
