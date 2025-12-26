package com.natrajtechnology.fitfly.data.service

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service for handling image uploads to Cloudinary
 */
object CloudinaryService {
    private const val TAG = "CloudinaryService"
    private const val CLOUD_NAME = "dncmn7api"
    private const val API_KEY = "412634157788419"
    private const val UPLOAD_PRESET = "fitness_app"  // Must be created in Cloudinary dashboard
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    
    private val httpClient = OkHttpClient()

    /**
     * Upload an image file to Cloudinary
     * @param context Android context for URI access
     * @param fileUri The URI of the file to upload
     * @param folder The folder in Cloudinary where the file will be stored
     * @return The URL of the uploaded file
     */
    suspend fun uploadImage(
        context: Context,
        fileUri: Uri,
        folder: String = "fitness-app/profiles"
    ): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            try {
                Log.d(TAG, "Starting upload for URI: $fileUri")

                // Get image data from URI
                val imageBytes = context.contentResolver.openInputStream(fileUri)?.readBytes()
                    ?: throw Exception("Unable to read file from URI")
                
                Log.d(TAG, "Image size: ${imageBytes.size} bytes")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        "profile_photo.jpg",
                        imageBytes.toRequestBody("image/jpeg".toMediaType())
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("folder", folder)
                    .addFormDataPart("resource_type", "image")
                    .addFormDataPart("quality", "auto:best")
                    .build()

                Log.d(TAG, "Request URL: $UPLOAD_URL")
                Log.d(TAG, "Upload Preset: $UPLOAD_PRESET")
                Log.d(TAG, "Folder: $folder")

                val request = Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBodyString = response.body?.string() ?: ""
                
                Log.d(TAG, "Response Code: ${response.code}")
                Log.d(TAG, "Response Body: $responseBodyString")
                
                if (response.isSuccessful) {
                    try {
                        val jsonResponse = JSONObject(responseBodyString)
                        
                        // Check for API error in response
                        if (jsonResponse.has("error")) {
                            val errorObj = jsonResponse.get("error")
                            val errorMessage = when (errorObj) {
                                is JSONObject -> errorObj.optString("message", "Unknown error")
                                is String -> errorObj
                                else -> errorObj.toString()
                            }
                            Log.e(TAG, "Cloudinary API Error: $errorMessage")
                            continuation.resumeWithException(Exception("Upload failed: $errorMessage"))
                            return@suspendCancellableCoroutine
                        }
                        
                        val secureUrl = jsonResponse.optString("secure_url", "")
                        val url = jsonResponse.optString("url", "")
                        val publicId = jsonResponse.optString("public_id", "")
                        
                        Log.d(TAG, "Secure URL: $secureUrl")
                        Log.d(TAG, "URL: $url")
                        Log.d(TAG, "Public ID: $publicId")
                        
                        val uploadedUrl = if (secureUrl.isNotEmpty()) secureUrl else url
                        if (uploadedUrl.isNotEmpty()) {
                            Log.d(TAG, "Upload successful: $uploadedUrl")
                            continuation.resume(Result.success(uploadedUrl))
                        } else {
                            Log.e(TAG, "No URL in response")
                            continuation.resumeWithException(Exception("Upload response missing URL"))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse response: ${e.message}")
                        continuation.resumeWithException(Exception("Failed to parse upload response: ${e.message}"))
                    }
                } else {
                    val errorMessage = "Upload failed with status ${response.code}: $responseBodyString"
                    Log.e(TAG, errorMessage)
                    continuation.resumeWithException(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload exception: ${e.message}", e)
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Get file name from URI
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                it.moveToFirst()
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else "photo.jpg"
            } ?: "photo.jpg"
        } catch (e: Exception) {
            "photo.jpg"
        }
    }
}
