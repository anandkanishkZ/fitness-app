package com.natrajtechnology.fitfly.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Workout Routine data model
 * Represents a complete workout routine with multiple exercises
 */
data class WorkoutRoutine(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val exerciseIds: List<String> = emptyList(),
    val requiredEquipment: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val imageUri: String? = null,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val geoTag: GeoTag? = null
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", emptyList(), emptyList(), false, null, "", 0L, null)
}
