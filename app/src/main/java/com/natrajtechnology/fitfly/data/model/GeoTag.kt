package com.natrajtechnology.fitfly.data.model

/**
 * GeoTag data model
 * Represents a geographical location (gym, yoga studio, park, etc.)
 */
data class GeoTag(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val locationType: LocationType = LocationType.GYM,
    val address: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this(0.0, 0.0, "", LocationType.GYM, "")
}

/**
 * Types of locations that can be tagged
 */
enum class LocationType {
    GYM,
    YOGA_STUDIO,
    PARK,
    HOME,
    OTHER
}
