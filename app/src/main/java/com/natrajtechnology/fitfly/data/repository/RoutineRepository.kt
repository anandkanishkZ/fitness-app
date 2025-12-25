package com.natrajtechnology.fitfly.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestore
import com.natrajtechnology.fitfly.data.model.WorkoutRoutine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing WorkoutRoutine data in Firestore
 */
class RoutineRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val routinesCollection = firestore.collection("routines")

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
    }

    /**
     * Get all routines for the current user as a Flow
     */
    fun getRoutines(): Flow<List<WorkoutRoutine>> = callbackFlow {
        var registration: ListenerRegistration? = null
        var activeUserId: String? = null

        fun detachListener() {
            registration?.remove()
            registration = null
            activeUserId = null
        }

        fun attachListener(userId: String) {
            Log.d("RoutineRepository", "Fetching routines for userId: $userId")
            activeUserId = userId
            registration = routinesCollection
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("RoutineRepository", "Error fetching routines", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val routines = snapshot?.documents?.mapNotNull { doc ->
                        val mapped = doc.toObject(WorkoutRoutine::class.java) ?: return@mapNotNull null

                        // Be tolerant to schema drift: some documents may use `completed` instead of `isCompleted`.
                        val completed = doc.getBoolean("isCompleted")
                            ?: doc.getBoolean("completed")
                            ?: mapped.isCompleted

                        mapped.copy(
                            id = doc.id,
                            isCompleted = completed
                        )
                    }?.sortedByDescending { it.createdAt } ?: emptyList()

                    Log.d("RoutineRepository", "Fetched ${routines.size} routines from Firestore")
                    trySend(routines)
                }
        }

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            if (userId.isNullOrBlank()) {
                detachListener()
                trySend(emptyList())
                return@AuthStateListener
            }

            if (activeUserId != userId) {
                detachListener()
                attachListener(userId)
            }
        }

        auth.addAuthStateListener(authListener)
        // Trigger initial attach/detach based on current auth state
        authListener.onAuthStateChanged(auth)

        awaitClose {
            detachListener()
            auth.removeAuthStateListener(authListener)
        }
    }

    /**
     * Get a single routine by ID
     */
    suspend fun getRoutineById(routineId: String): Result<WorkoutRoutine> {
        return try {
            val document = routinesCollection.document(routineId).get().await()
            val mapped = document.toObject(WorkoutRoutine::class.java)
            val routine = mapped?.let {
                val completed = document.getBoolean("isCompleted")
                    ?: document.getBoolean("completed")
                    ?: it.isCompleted
                it.copy(id = document.id, isCompleted = completed)
            }
            if (routine != null) {
                Result.success(routine)
            } else {
                Result.failure(Exception("Routine not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add a new routine
     */
    suspend fun addRoutine(routine: WorkoutRoutine): Result<String> {
        return try {
            val userId = getCurrentUserId()
            val routineWithUser = routine.copy(userId = userId)
            val documentRef = routinesCollection.add(routineWithUser).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing routine
     */
    suspend fun updateRoutine(routine: WorkoutRoutine): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val routineWithUser = if (routine.userId.isEmpty()) {
                routine.copy(userId = userId)
            } else {
                routine
            }
            routinesCollection.document(routine.id).set(routineWithUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a routine
     */
    suspend fun deleteRoutine(routineId: String): Result<Unit> {
        return try {
            routinesCollection.document(routineId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle completion status of a routine
     */
    suspend fun toggleRoutineCompletion(routineId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            val newValue = !isCompleted
            android.util.Log.d(
                "RoutineRepository",
                "Toggling routine $routineId from $isCompleted to $newValue"
            )
            routinesCollection.document(routineId)
                // Write both fields to keep old/new clients consistent.
                .update(
                    mapOf(
                        "isCompleted" to newValue,
                        "completed" to newValue
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("RoutineRepository", "Error toggling completion", e)
            Result.failure(e)
        }
    }
}
