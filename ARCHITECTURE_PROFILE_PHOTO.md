# 🏗️ Profile Photo Upload Architecture

## System Design Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        ANDROID DEVICE                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   UI LAYER (Compose)                      │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │           ProfileScreen.kt                         │  │   │
│  │  │  - Avatar Circle (CLICKABLE) ✅                    │  │   │
│  │  │  - "+" Button (CLICKABLE) ✅                       │  │   │
│  │  │  - "Choose Photo" Button                           │  │   │
│  │  │  - Displays: selectedPhotoUri                      │  │   │
│  │  │  - Shows: photoUploadUiState (Loading/Success)    │  │   │
│  │  └────────────────┬─────────────────────────────────┘  │   │
│  │                   │ onPickImage()                       │   │
│  │                   ▼                                     │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │         MainActivity.kt                           │  │   │
│  │  │  - pickImageLauncher                             │  │   │
│  │  │  - ActivityResultContracts.GetContent()          │  │   │
│  │  │  - Launches: pickImageLauncher.launch("image/*") │  │   │
│  │  │  - Receives: Uri from gallery                    │  │   │
│  │  │  - Calls: authViewModel.updateProfilePhoto(uri)  │  │   │
│  │  └────────────────┬─────────────────────────────────┘  │   │
│  └───────────────────┼──────────────────────────────────────┘  │
│                      │                                          │
│  ┌───────────────────▼──────────────────────────────────────┐  │
│  │            VIEWMODEL LAYER (StateFlow)                   │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │         AuthViewModel.kt                           │  │  │
│  │  │  - photoUploadUiState: StateFlow<UiState>         │  │  │
│  │  │  - updateProfilePhoto(photoUri: Uri)              │  │  │
│  │  │    1. Sets state to LOADING                        │  │  │
│  │  │    2. Calls authRepository.updateProfilePhoto()   │  │  │
│  │  │    3. On Success: Updates _currentUser            │  │  │
│  │  │    4. On Error: Sets error state                  │  │  │
│  │  │  - Uses: AuthViewModelFactory for DI              │  │  │
│  │  └────────────────┬─────────────────────────────────┘  │  │
│  └───────────────────┼──────────────────────────────────────┘  │
│                      │ authRepository.updateProfilePhoto()     │
│                      ▼                                          │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │            REPOSITORY LAYER (Data Access)               │ │
│  │  ┌─────────────────────────────────────────────────────┐ │ │
│  │  │       AuthRepository.kt                            │ │ │
│  │  │  - updateProfilePhoto(photoUri: Uri)               │ │ │
│  │  │  - Receives: Context from constructor              │ │ │
│  │  │  - Calls: CloudinaryService.uploadImage()          │ │ │
│  │  │  - On Success:                                     │ │ │
│  │  │    1. Gets Cloudinary URL                          │ │ │
│  │  │    2. Updates Firebase user.updateProfile()        │ │ │
│  │  │    3. Refreshes currentUser                        │ │ │
│  │  │    4. Returns: Pair<String, FirebaseUser>          │ │ │
│  │  │  - Context provided via constructor injection      │ │ │
│  │  └─────────────────┬─────────────────────────────────┘ │ │
│  └────────────────────┼─────────────────────────────────────┘ │
│                       │ CloudinaryService.uploadImage()       │
│                       ▼                                        │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │           SERVICE LAYER (External Integration)          │ │
│  │  ┌─────────────────────────────────────────────────────┐ │ │
│  │  │      CloudinaryService.kt                          │ │ │
│  │  │  uploadImage(context, fileUri, folder)             │ │ │
│  │  │                                                     │ │ │
│  │  │  STEP 1: Read Image Bytes                          │ │ │
│  │  │  ├─ context.contentResolver.openInputStream(uri)  │ │ │
│  │  │  ├─ .readBytes()  ← Critical fix!                 │ │ │
│  │  │  └─ Supports: content://, file://, etc.           │ │ │
│  │  │                                                     │ │ │
│  │  │  STEP 2: Build Multipart Request                   │ │ │
│  │  │  ├─ file: image bytes                              │ │ │
│  │  │  ├─ upload_preset: "fitness_app"                   │ │ │
│  │  │  ├─ folder: "fitness-app/profiles"                 │ │ │
│  │  │  └─ resource_type: "image"                         │ │ │
│  │  │                                                     │ │ │
│  │  │  STEP 3: Send to Cloudinary                        │ │ │
│  │  │  ├─ URL: https://api.cloudinary.com/v1_1/...      │ │ │
│  │  │  ├─ Method: POST multipart/form-data               │ │ │
│  │  │  └─ Client: OkHttp                                 │ │ │
│  │  │                                                     │ │ │
│  │  │  STEP 4: Parse Response                            │ │ │
│  │  │  ├─ Extract: secure_url (HTTPS preferred)          │ │ │
│  │  │  ├─ Fallback: url (HTTP)                           │ │ │
│  │  │  └─ Return: Result<String>                         │ │ │
│  │  │                                                     │ │ │
│  │  │  Returns: String (HTTPS URL to uploaded image)    │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP Request
                              ▼
        ┌──────────────────────────────────────┐
        │     CLOUDINARY API (Cloud)           │
        │  - Receives: Image bytes             │
        │  - Validates: upload_preset          │
        │  - Stores: To CDN                    │
        │  - Applies: Auto-optimization        │
        │  - Returns: secure_url (HTTPS)       │
        └──────────────────────────────────────┘
                              │
                              │ HTTPS Response
                              ▼
                  ┌──────────────────────────────┐
                  │   CLOUDINARY CDN             │
                  │   (Secure Image Storage)     │
                  │   dncmn7api.cloudinary.com   │
                  └──────────────────────────────┘
                              │
                              │ Display URL
                              ▼
                  ┌──────────────────────────────┐
                  │   Coil AsyncImage            │
                  │   (Displays in Avatar)       │
                  └──────────────────────────────┘
```

---

## Data Flow: Complete Journey

### 1️⃣ User Initiates Upload
```
ProfileScreen
  └─ User taps avatar OR taps "+" button
     └─ onPickImage() callback fires
        └─ Calls: MainActivity's pickImageLauncher.launch("image/*")
```

### 2️⃣ System Launches Gallery
```
ActivityResultContracts.GetContent()
  └─ Opens Android native gallery/file picker
     └─ User selects image
        └─ System returns: Uri (content://media/external/images/...)
           └─ MainActivity.pickImageLauncher receives Uri
```

### 3️⃣ ViewModel Manages State
```
MainActivity
  └─ authViewModel.updateProfilePhoto(uri)
     └─ AuthViewModel.updateProfilePhoto()
        └─ Sets: _photoUploadUiState = Loading
           └─ Launches coroutine
              └─ Calls: authRepository.updateProfilePhoto(uri)
```

### 4️⃣ Repository Orchestrates Upload
```
AuthRepository.updateProfilePhoto(uri)
  └─ Receives: Uri (from ViewModel)
     └─ Has: Context (from constructor - injected by AuthViewModelFactory)
        └─ Calls: CloudinaryService.uploadImage(context, uri, folder)
           └─ Awaits: Result<String>
              └─ On Success:
                 ├─ Gets: CloudinaryUrl (secure_url)
                 └─ Calls: Firebase user.updateProfile(displayName, photoUri)
              └─ On Failure:
                 └─ Throws: Exception with error message
```

### 5️⃣ Service Handles HTTP Upload
```
CloudinaryService.uploadImage(context, fileUri, folder)
  └─ STEP 1: Read URI
     ├─ context.contentResolver.openInputStream(fileUri)
     ├─ Reads entire image as ByteArray
     └─ Supports: ALL Uri schemes (content://, file://, etc.)
  
  └─ STEP 2: Create Multipart Body
     ├─ file: [image bytes] (application/jpeg)
     ├─ upload_preset: "fitness_app"
     ├─ folder: "fitness-app/profiles"
     ├─ resource_type: "image"
     ├─ quality: "auto:best"
     └─ Built with: OkHttp3 MultipartBody.Builder
  
  └─ STEP 3: Send HTTP Request
     ├─ POST to: https://api.cloudinary.com/v1_1/dncmn7api/image/upload
     ├─ Client: OkHttp3 (configured in object)
     └─ Executes synchronously (wrapped in coroutine)
  
  └─ STEP 4: Parse Response
     ├─ Checks: response.isSuccessful
     ├─ Parses: JSON response body
     ├─ Extracts: secure_url (HTTPS)
     ├─ Fallback: url (HTTP)
     └─ Returns: Result<String> (URL)
```

### 6️⃣ Update Firebase Profile
```
Firebase Authentication
  └─ user.updateProfile {
     ├─ photoUri = cloudinaryUrl
     └─ displayName = existing
  }
     └─ Syncs to: Firebase Cloud
        └─ Updates: currentUser reference
           └─ AuthViewModel receives updated user
```

### 7️⃣ UI Reflects Changes
```
AuthViewModel
  └─ Receives: Updated FirebaseUser with new photoUrl
     └─ Updates: _currentUser StateFlow
        └─ Sets: _photoUploadUiState = Success
           └─ ProfileScreen collects state
              └─ Displays: Photo in avatar (AsyncImage)
                 └─ Shows: "Profile photo updated" snackbar
```

---

## Key Technical Decisions

### Why ContentResolver.openInputStream() Instead of File?
```kotlin
// ❌ WRONG: Android content URIs don't convert to File paths
val file = File(uri.path) // Returns null or invalid path!

// ✅ RIGHT: Read bytes directly from URI
val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
// Works with: content://, file://, http://, all schemes!
```

### Why Upload Preset Instead of API Key?
```kotlin
// ❌ API Key exposed in client (security risk)
.addFormDataPart("api_key", "412634157788419")

// ✅ Unsigned preset (no secrets needed)
.addFormDataPart("upload_preset", "fitness_app")
// More secure, follows Cloudinary best practices
```

### Why Factory Pattern for Context?
```kotlin
// ❌ Context not available in ViewModel
class AuthViewModel : ViewModel() {
    val repository = AuthRepository()  // No context!
}

// ✅ Factory provides context through DI
class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(context) as T
    }
}

// Usage in MainActivity
viewModel(factory = AuthViewModelFactory(this@MainActivity))
```

---

## State Management Flow

```
UiState Sealed Class:
├─ Initial
│  └─ Default state, no upload in progress
├─ Loading
│  └─ Upload in progress
│  └─ Progress indicator shown
│  └─ Buttons disabled
├─ Success(message: String)
│  └─ Upload completed successfully
│  └─ Snackbar shown with message
│  └─ Photo displayed in avatar
└─ Error(message: String)
   └─ Upload failed
   └─ Error snackbar shown
   └─ User can retry

Flow Through ViewModel:
Initial
  └─ User taps avatar/button
     └─ authViewModel.updateProfilePhoto(uri)
        └─ State: Loading
           └─ Upload in progress...
              └─ (CloudinaryService works, 5-10 seconds)
                 └─ On Success: State = Success("Profile photo updated")
                 └─ On Failure: State = Error("Upload failed: ...")
                    └─ Snackbar shown
                       └─ clearPhotoUploadUiState()
                          └─ State: Initial (ready for next upload)
```

---

## Error Handling Strategy

```
Upload Can Fail At:

1. URI Reading
   └─ contentResolver.openInputStream() returns null
      └─ Error: "Unable to read file from URI"

2. Network
   └─ No internet connection
      └─ Error: "Network error" (OkHttp throws)

3. Cloudinary API
   └─ Invalid upload_preset
   └─ File too large
   └─ API rate limit
      └─ Error: Cloudinary error message (from JSON response)

4. Firebase
   └─ User not authenticated
   └─ Network issue during sync
      └─ Error: Firebase exception message

All Errors:
  └─ Caught in AuthViewModel.updateProfilePhoto()
     └─ Set: _photoUploadUiState = Error(message)
        └─ Displayed in: Snackbar to user
           └─ User can retry immediately
```

---

## Component Responsibilities

| Component | Responsibility |
|-----------|-----------------|
| **ProfileScreen** | Display UI, handle user interactions, show state |
| **MainActivity** | Launch image picker, route callbacks, DI setup |
| **AuthViewModel** | State management, orchestrate repository calls |
| **AuthViewModelFactory** | Provide context to ViewModel (DI) |
| **AuthRepository** | Coordinate upload + Firebase update |
| **CloudinaryService** | HTTP upload to Cloudinary API |
| **Cloudinary API** | Store image, return HTTPS URL |
| **AsyncImage (Coil)** | Display image from URL in UI |

---

## Testing Scenarios

### Scenario 1: Happy Path
```
1. User taps avatar
2. Gallery opens
3. User selects photo
4. Loading spinner shows
5. Upload completes (5-10s)
6. Photo appears in avatar
7. Success snackbar shown
8. New photo persists on app restart
```

### Scenario 2: Multiple Uploads
```
1. User uploads Photo A
2. Success shown
3. User uploads Photo B
4. Old Photo A replaced with B
5. Success shown
6. Only Photo B persists
```

### Scenario 3: Network Failure
```
1. User taps avatar
2. Gallery opens
3. User selects photo
4. Upload fails (no internet)
5. Error snackbar shown
6. User can retry immediately
```

### Scenario 4: Offline Then Online
```
1. User selects photo (offline)
2. Upload fails (no connection)
3. Error shown
4. User enables WiFi/mobile
5. User taps avatar again
6. Upload succeeds
7. Success shown
```

---

## Security Considerations

✅ **Unsigned Upload Preset**
- No API key exposed in client code
- Follows Cloudinary best practices

✅ **HTTPS URLs**
- Images always served over HTTPS
- `secure_url` preferred over `url`

✅ **ContentResolver URI Access**
- Uses Android's secure file access mechanism
- Respects user's storage permissions

✅ **Firebase Authentication**
- Only authenticated users can update profile
- Firebase enforces security rules

✅ **Upload Folder Isolation**
- All photos stored in: `fitness-app/profiles/`
- Easy to manage and clean up

---

## Performance Characteristics

| Operation | Time | Notes |
|-----------|------|-------|
| Gallery Open | 0.5-2s | Device dependent |
| Image Selection | Instant | Local |
| Upload (5MB image) | 5-10s | Network dependent |
| Firebase Sync | 1-2s | Firebase |
| UI Update | <100ms | Instant |

---

**Last Updated:** December 26, 2025
**Status:** ✅ PRODUCTION READY
