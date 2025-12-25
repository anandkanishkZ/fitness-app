package com.natrajtechnology.fitfly.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestore
import com.natrajtechnology.fitfly.data.model.Exercise
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing Exercise data in Firestore
 */
class ExerciseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val exercisesCollection = firestore.collection("exercises")

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
    }

    /**
     * Get all exercises for the current user as a Flow
     */
    fun getExercises(): Flow<List<Exercise>> = callbackFlow {
        var registration: ListenerRegistration? = null
        var activeUserId: String? = null

        fun detachListener() {
            registration?.remove()
            registration = null
            activeUserId = null
        }

        fun attachListener(userId: String) {
            Log.d("ExerciseRepository", "Fetching exercises for userId: $userId")
            activeUserId = userId
            registration = exercisesCollection
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ExerciseRepository", "Error fetching exercises", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val exercises = snapshot?.documents?.mapNotNull { doc ->
                        val mapped = doc.toObject(Exercise::class.java) ?: return@mapNotNull null

                        // Be tolerant to schema drift: some documents may use `completed` instead of `isCompleted`.
                        val completed = doc.getBoolean("isCompleted")
                            ?: doc.getBoolean("completed")
                            ?: mapped.isCompleted

                        mapped.copy(
                            id = doc.id,
                            isCompleted = completed
                        )
                    }?.sortedByDescending { it.createdAt } ?: emptyList()

                    Log.d("ExerciseRepository", "Fetched ${exercises.size} exercises from Firestore")
                    trySend(exercises)
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
     * Get a single exercise by ID
     */
    suspend fun getExerciseById(exerciseId: String): Result<Exercise> {
        return try {
            val document = exercisesCollection.document(exerciseId).get().await()
            val mapped = document.toObject(Exercise::class.java)
            val exercise = mapped?.let {
                val completed = document.getBoolean("isCompleted")
                    ?: document.getBoolean("completed")
                    ?: it.isCompleted
                it.copy(id = document.id, isCompleted = completed)
            }
            if (exercise != null) {
                Result.success(exercise)
            } else {
                Result.failure(Exception("Exercise not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add a new exercise
     */
    suspend fun addExercise(exercise: Exercise): Result<String> {
        return try {
            val userId = getCurrentUserId()
            val exerciseWithUser = exercise.copy(userId = userId)
            val documentRef = exercisesCollection.add(exerciseWithUser).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing exercise
     */
    suspend fun updateExercise(exercise: Exercise): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val exerciseWithUser = if (exercise.userId.isEmpty()) {
                exercise.copy(userId = userId)
            } else {
                exercise
            }
            exercisesCollection.document(exercise.id).set(exerciseWithUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete an exercise
     */
    suspend fun deleteExercise(exerciseId: String): Result<Unit> {
        return try {
            exercisesCollection.document(exerciseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle completion status of an exercise
     */
    suspend fun toggleExerciseCompletion(exerciseId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            val newValue = !isCompleted
            android.util.Log.d(
                "ExerciseRepository",
                "Toggling exercise $exerciseId from $isCompleted to $newValue"
            )
            exercisesCollection.document(exerciseId)
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
            android.util.Log.e("ExerciseRepository", "Error toggling completion", e)
            Result.failure(e)
        }
    }
}
