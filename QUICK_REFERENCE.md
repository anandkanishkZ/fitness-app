# FitLife - Quick Reference & Troubleshooting

## 🚀 Quick Start Commands

### Build Project
```bash
./gradlew clean build
```

### Install on Device
```bash
./gradlew installDebug
```

### Run Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Check Dependencies
```bash
./gradlew app:dependencies
```

## 📁 Project Structure Quick Reference

```
app/src/main/java/com/natrajtechnology/fitfly/
├── MainActivity.kt                    # Entry point
├── data/
│   ├── model/
│   │   ├── Exercise.kt               # Exercise data class
│   │   ├── WorkoutRoutine.kt         # Routine data class
│   │   └── User.kt                   # User data class
│   └── repository/
│       ├── AuthRepository.kt         # Authentication logic
│       ├── ExerciseRepository.kt     # Exercise CRUD
│       └── RoutineRepository.kt      # Routine CRUD
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt             # Landing page
│   │   ├── LoginScreen.kt            # Login UI
│   │   ├── SignUpScreen.kt           # Registration UI
│   │   ├── DashboardScreen.kt        # Main app screen
│   │   ├── AddEditExerciseScreen.kt  # Exercise CRUD UI
│   │   ├── AddEditRoutineScreen.kt   # Routine CRUD UI
│   │   └── RoutineDetailScreen.kt    # Routine details + SMS
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt          # Auth state management
│   │   ├── ExerciseViewModel.kt      # Exercise state
│   │   └── RoutineViewModel.kt       # Routine state
│   └── theme/
│       ├── Color.kt                  # App colors
│       ├── Theme.kt                  # Material theme
│       └── Type.kt                   # Typography
└── navigation/
    ├── Screen.kt                     # Route definitions
    └── AppNavigation.kt              # Navigation graph
```

## 🔥 Firebase Quick Reference

### Collections Structure

**exercises**
```kotlin
{
  id: String (auto)
  name: String
  sets: Int
  reps: Int
  instructions: String
  requiredEquipment: List<String>
  isCompleted: Boolean
  userId: String
  createdAt: Long
}
```

**routines**
```kotlin
{
  id: String (auto)
  name: String
  description: String
  exerciseIds: List<String>
  requiredEquipment: List<String>
  isCompleted: Boolean
  userId: String
  createdAt: Long
}
```

### Firestore Rules (Copy-Paste Ready)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /exercises/{exerciseId} {
      allow read, write: if request.auth != null && 
                           resource.data.userId == request.auth.uid;
      allow create: if request.auth != null;
    }
    
    match /routines/{routineId} {
      allow read, write: if request.auth != null && 
                           resource.data.userId == request.auth.uid;
      allow create: if request.auth != null;
    }
  }
}
```

## 🐛 Common Issues & Solutions

### Issue 1: "Default FirebaseApp is not initialized"

**Symptoms**: App crashes on startup with Firebase error

**Solutions**:
1. Check `google-services.json` exists in `app/` folder
2. Verify package name matches: `com.natrajtechnology.fitfly`
3. Clean and rebuild:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```
4. Invalidate caches: File → Invalidate Caches → Restart

---

### Issue 2: Authentication Fails

**Symptoms**: Login/signup doesn't work, shows error

**Solutions**:
1. Firebase Console → Authentication → Enable Email/Password
2. Check internet connection
3. Verify Firestore rules allow authenticated users
4. Check Firebase logs in console
5. Try with different email (might be blocked)

---

### Issue 3: Gradle Sync Failed

**Symptoms**: Red errors in build files, can't compile

**Solutions**:
1. Update Android Studio to latest
2. Update Gradle wrapper:
   ```bash
   ./gradlew wrapper --gradle-version=8.0
   ```
3. Check `gradle/libs.versions.toml` for version conflicts
4. Delete `.gradle` folder and sync again
5. File → Sync Project with Gradle Files

---

### Issue 4: Firestore Permission Denied

**Symptoms**: Data not loading, "permission-denied" error

**Solutions**:
1. Check user is logged in (`currentUser != null`)
2. Verify Firestore rules are set correctly
3. Check `userId` field is populated in documents
4. In Firebase Console, temporarily set test mode:
   ```javascript
   allow read, write: if true; // TEST ONLY
   ```
5. Check Firebase Console → Firestore logs

---

### Issue 5: Shake Gesture Not Working

**Symptoms**: Shaking device does nothing

**Solutions**:
1. **Use physical device** (emulator sensors unreliable)
2. Check permission in AndroidManifest (not required for accelerometer)
3. Shake harder (threshold is 15 m/s²)
4. Wait 2 seconds between shakes (cooldown)
5. Check logcat for sensor availability:
   ```kotlin
   sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
   ```

---

### Issue 6: SMS Not Opening

**Symptoms**: Click Send SMS, nothing happens

**Solutions**:
1. Check SMS permission in AndroidManifest
2. Verify phone number is not empty
3. Test on physical device (emulator might not have SMS app)
4. Check intent format:
   ```kotlin
   Intent(Intent.ACTION_SENDTO).apply {
       data = Uri.parse("smsto:$phoneNumber")
       putExtra("sms_body", message)
   }
   ```

---

### Issue 7: Build Takes Too Long

**Symptoms**: Gradle build very slow (>5 minutes)

**Solutions**:
1. Enable Gradle daemon in `gradle.properties`:
   ```properties
   org.gradle.daemon=true
   org.gradle.parallel=true
   org.gradle.caching=true
   ```
2. Increase RAM allocation:
   ```properties
   org.gradle.jvmargs=-Xmx4096m
   ```
3. Disable unnecessary features during development
4. Use `./gradlew assemble` instead of `build`

---

### Issue 8: App Crashes on Rotation

**Symptoms**: App crashes when device rotates

**Solutions**:
1. ViewModels handle state automatically (should work)
2. Check for unhandled null states
3. Add to Activity in AndroidManifest if needed:
   ```xml
   android:configChanges="orientation|screenSize"
   ```
4. Ensure Flow collectors are lifecycle-aware

---

### Issue 9: Data Not Persisting

**Symptoms**: Data disappears after app restart

**Solutions**:
1. Check Firebase save operations complete
2. Verify `await()` is used on async operations
3. Check Firestore Console for actual data
4. Enable Firestore offline persistence (already enabled by default)
5. Check for errors in repository methods

---

### Issue 10: UI Not Updating

**Symptoms**: Changes in data don't reflect in UI

**Solutions**:
1. Verify using `StateFlow` and `collectAsState()`
2. Check Flow collection in ViewModel
3. Ensure mutations trigger new Flow emissions
4. Use `remember` for local state
5. Check Recomposition scope

---

## ⚡ Performance Tips

### 1. Optimize Firestore Queries
```kotlin
// Good: Filter at database level
exercisesCollection
    .whereEqualTo("userId", userId)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .addSnapshotListener { ... }

// Bad: Load all then filter
exercisesCollection.addSnapshotListener { ... }
    .filter { it.userId == userId }
```

### 2. Use LazyColumn for Lists
```kotlin
// Already implemented
LazyColumn {
    items(exercises, key = { it.id }) { exercise ->
        ExerciseItem(exercise)
    }
}
```

### 3. Debounce Text Input
```kotlin
var searchQuery by remember { mutableStateOf("") }
LaunchedEffect(searchQuery) {
    delay(300) // Wait 300ms after typing stops
    // Perform search
}
```

## 🎨 Customization Quick Tips

### Change App Theme Colors
**File**: `ui/theme/Color.kt`
```kotlin
val Purple80 = Color(0xFF6200EE)  // Your primary color
val PurpleGrey80 = Color(0xFF3700B3)  // Your secondary color
```

### Change App Name
**File**: `app/src/main/res/values/strings.xml`
```xml
<string name="app_name">YourAppName</string>
```

### Change Package Name
1. Refactor package in Android Studio
2. Update in `build.gradle.kts` → `applicationId`
3. Update Firebase `google-services.json`
4. Update in `AndroidManifest.xml`

### Add New Screen
1. Create Composable in `ui/screens/`
2. Add route in `navigation/Screen.kt`
3. Add composable to `navigation/AppNavigation.kt`
4. Navigate using: `navController.navigate(Screen.NewScreen.route)`

## 📱 Testing Checklist

### Manual Test Cases

**Authentication**
- [ ] Register with valid email/password
- [ ] Register with invalid email (should fail)
- [ ] Register with short password (should fail)
- [ ] Login with correct credentials
- [ ] Login with wrong password (should fail)
- [ ] Logout and login again

**Exercises**
- [ ] Create exercise with all fields
- [ ] Create exercise with minimal fields
- [ ] Edit exercise name
- [ ] Edit sets and reps
- [ ] Add equipment
- [ ] Remove equipment
- [ ] Delete exercise (with confirmation)
- [ ] Mark exercise complete
- [ ] Unmark exercise

**Routines**
- [ ] Create routine with exercises
- [ ] Create routine without exercises
- [ ] Add exercises to routine
- [ ] Remove exercises from routine
- [ ] View routine details
- [ ] Edit routine
- [ ] Delete routine (with confirmation)
- [ ] Mark routine complete

**SMS Delegation**
- [ ] Open routine detail
- [ ] Click SMS button
- [ ] Enter phone number
- [ ] Add optional note
- [ ] Verify message format
- [ ] Send via SMS app

**Gestures**
- [ ] Long press to delete
- [ ] Tap checkbox to complete
- [ ] Shake device to reset
- [ ] Confirm reset dialog

## 📊 LogCat Filters

### View Firebase Logs Only
```
tag:FirebaseAuth | tag:Firestore
```

### View App Logs Only
```
package:com.natrajtechnology.fitfly
```

### View Errors Only
```
level:ERROR
```

### View Navigation Events
```
tag:NavController
```

## 🔗 Useful Links

- **Firebase Console**: https://console.firebase.google.com/
- **Material Design**: https://m3.material.io/
- **Compose Documentation**: https://developer.android.com/jetpack/compose
- **Kotlin Coroutines**: https://kotlinlang.org/docs/coroutines-guide.html
- **Navigation Compose**: https://developer.android.com/jetpack/compose/navigation

## 📞 Emergency Fixes

### If App Won't Build at All
```bash
# Nuclear option - reset everything
rm -rf .gradle build
./gradlew clean
./gradlew build --refresh-dependencies
```

### If Firebase Not Working
```bash
# Re-download google-services.json from Firebase Console
# Replace in app/ folder
# Sync Gradle
# Clean and rebuild
```

### If Emulator Not Starting
```bash
# From Android Studio Terminal
emulator -avd Pixel_5_API_30 -wipe-data
```

---

## ✅ Pre-Submission Checklist

- [ ] All features working as per requirements
- [ ] No crashes in logcat
- [ ] Firebase properly configured
- [ ] All permissions granted
- [ ] Test data populated
- [ ] App name is "FitLife"
- [ ] Screenshots taken
- [ ] README.md reviewed
- [ ] Code commented adequately
- [ ] Demo script prepared

---

**Keep this file handy during development and demo!** 🚀
