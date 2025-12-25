package com.natrajtechnology.fitfly.data.model

/**
 * User data model
 * Represents a user in the application
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", 0L)
}
