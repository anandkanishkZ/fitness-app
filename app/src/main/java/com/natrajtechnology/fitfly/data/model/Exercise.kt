package com.natrajtechnology.fitfly.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Exercise data model
 * Represents a single exercise in a workout routine
 */
data class Exercise(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val instructions: String = "",
    val requiredEquipment: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val imageUri: String? = null,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val geoTag: GeoTag? = null
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", 0, 0, "", emptyList(), false, null, "", 0L, null)
}
