# FitLife Setup Guide

## 📋 Prerequisites Checklist

Before you start, ensure you have:

- ✅ Android Studio (Arctic Fox or newer)
- ✅ JDK 11 or higher
- ✅ Android SDK with API 24+ installed
- ✅ A Google/Firebase account
- ✅ Physical Android device or emulator (Android 7.0+)

## 🔥 Firebase Configuration (IMPORTANT!)

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or select existing project
3. Enter project name: `FitLife` (or your preferred name)
4. Click "Continue" and follow the setup wizard
5. Enable Google Analytics (optional)
6. Click "Create project"

### Step 2: Add Android App to Firebase

1. In Firebase Console, click the Android icon ⚙️
2. Enter package name: **`com.natrajtechnology.fitfly`** (must match exactly)
3. Enter app nickname: `FitLife`
4. Leave SHA-1 empty for now (optional for Authentication)
5. Click "Register app"

### Step 3: Download Configuration File

1. Download `google-services.json` file
2. **IMPORTANT**: Replace the placeholder file at:
   ```
   app/google-services.json
   ```
3. The file should contain your actual Firebase project credentials

### Step 4: Enable Firebase Authentication

1. In Firebase Console, go to **Authentication** section
2. Click "Get Started"
3. Go to **Sign-in method** tab
4. Enable **Email/Password** provider:
   - Click on "Email/Password"
   - Toggle "Enable"
   - Click "Save"

### Step 5: Enable Firestore Database

1. In Firebase Console, go to **Firestore Database** section
2. Click "Create database"
3. Select **Test mode** (for development):
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```
4. Choose a Firestore location (e.g., `us-central1`)
5. Click "Enable"

### Step 6: Firestore Security Rules (Production Ready)

For production, update Firestore rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Exercises collection - users can only access their own exercises
    match /exercises/{exerciseId} {
      allow read, write: if request.auth != null && 
                           resource.data.userId == request.auth.uid;
      allow create: if request.auth != null;
    }
    
    // Routines collection - users can only access their own routines
    match /routines/{routineId} {
      allow read, write: if request.auth != null && 
                           resource.data.userId == request.auth.uid;
      allow create: if request.auth != null;
    }
  }
}
```

## 🛠️ Android Studio Setup

### Step 1: Open Project

1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to the project folder
4. Click "OK"

### Step 2: Sync Gradle

1. Wait for Android Studio to index the project
2. When prompted, click "Sync Now"
3. Wait for Gradle sync to complete (may take several minutes first time)

### Step 3: Verify Configuration

Check that these files exist:
- ✅ `app/google-services.json` (your Firebase config)
- ✅ `app/build.gradle.kts` (contains google-services plugin)
- ✅ `gradle/libs.versions.toml` (dependencies defined)

## ▶️ Running the App

### Option 1: Using Android Emulator

1. In Android Studio, click **Tools → Device Manager**
2. Create a new virtual device:
   - Click "Create device"
   - Select "Phone" → "Pixel 5" (recommended)
   - Select System Image: API 30+ (Android 11+)
   - Click "Finish"
3. Click the green **Run** button ▶️
4. Select your emulator
5. Wait for app to build and launch

### Option 2: Using Physical Device

1. Enable Developer Options on your phone:
   - Go to Settings → About Phone
   - Tap "Build number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"
2. Connect phone via USB
3. Allow USB debugging on phone when prompted
4. In Android Studio, click **Run** ▶️
5. Select your device
6. App will install and launch

## 🧪 Testing the App

### Test User Registration

1. Launch app
2. Click "Sign Up"
3. Enter details:
   - Name: Test User
   - Email: test@example.com
   - Password: test123456
4. Click "Sign Up"
5. Should navigate to Dashboard

### Test Exercise Creation

1. On Dashboard, go to "Exercises" tab
2. Click the "+" FAB button
3. Enter exercise details:
   - Name: Push-ups
   - Sets: 3
   - Reps: 15
   - Instructions: Keep back straight
   - Equipment: Mat
4. Click "Add Exercise"
5. Exercise should appear in list

### Test Routine Creation

1. On Dashboard, go to "Routines" tab
2. Click the "+" FAB button
3. Enter routine details:
   - Name: Morning Workout
   - Description: Quick 15-minute routine
4. Click "Select Exercises"
5. Check exercises to include
6. Click "Create Routine"
7. Routine should appear in list

### Test Gesture Controls

1. **Swipe Left** on any exercise/routine → Shows delete option
2. **Swipe Right** on any exercise/routine → Marks as complete
3. **Shake Device** → Shows reset dialog

### Test SMS Delegation

1. Click on any routine
2. Click SMS icon in top bar
3. Enter phone number
4. Add optional note
5. Click "Send SMS"
6. SMS app opens with pre-filled message
7. Review and send manually

## 🔍 Troubleshooting

### Build Errors

**Issue**: "Plugin with id 'com.google.gms.google-services' not found"
**Solution**: 
1. Check `build.gradle.kts` (project level) has google-services classpath
2. Run `./gradlew clean` then rebuild

**Issue**: "Default FirebaseApp is not initialized"
**Solution**:
1. Verify `google-services.json` is in `app/` folder
2. Check package name matches exactly
3. Clean and rebuild project

**Issue**: Gradle sync failed
**Solution**:
1. Update Android Studio to latest version
2. File → Invalidate Caches → Invalidate and Restart
3. Delete `.gradle` folder and sync again

### Firebase Errors

**Issue**: "FirebaseException: Permission denied"
**Solution**:
1. Check Firestore rules allow authenticated users
2. Verify user is logged in
3. Check userId field is set correctly

**Issue**: Authentication fails
**Solution**:
1. Verify Email/Password provider is enabled in Firebase Console
2. Check internet connection
3. Review Firebase Authentication logs

### Runtime Errors

**Issue**: App crashes on startup
**Solution**:
1. Check logcat for error messages
2. Verify all permissions in AndroidManifest.xml
3. Ensure Firebase is initialized correctly

**Issue**: Gestures not working
**Solution**:
1. For shake: Device must have accelerometer sensor
2. For swipe: Ensure using physical device or emulator with touch support

## 📱 Permissions Setup

The app requires these permissions (auto-granted on install):

```xml
<!-- Required for Firebase -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Required for SMS feature -->
<uses-permission android:name="android.permission.SEND_SMS" />

<!-- Optional for contacts picker -->
<uses-permission android:name="android.permission.READ_CONTACTS" />
```

## 🎯 Verification Checklist

Before submitting/demonstrating, verify:

- ✅ Firebase configuration is correct
- ✅ User can register and login
- ✅ Can create, edit, delete exercises
- ✅ Can create, edit, delete routines
- ✅ Can mark items as complete
- ✅ Gesture controls work (swipe, shake)
- ✅ SMS delegation works
- ✅ App persists data (logout and login again)
- ✅ UI is responsive and follows Material Design
- ✅ No crashes or errors in logcat

## 📊 Testing Data

Use these sample data for demonstration:

**Exercises:**
1. Push-ups (3×15, Equipment: Mat)
2. Squats (4×20, Equipment: None)
3. Dumbbell Press (3×12, Equipment: Dumbbells)
4. Plank (3×60s, Equipment: Mat)

**Routines:**
1. Morning Routine (Push-ups, Squats)
2. Upper Body Day (Push-ups, Dumbbell Press)
3. Full Body Workout (All exercises)

## 🎓 Demo Script for Assessment

1. **Show Home Screen** (clean Material Design)
2. **Register new user** (input validation working)
3. **Add 3-4 exercises** (CRUD operations)
4. **Create 2 routines** (with equipment checklist)
5. **Mark exercises complete** (visual feedback)
6. **Use swipe gestures** (delete & complete)
7. **Shake device** (reset dialog)
8. **Send SMS** (pre-filled message)
9. **Logout and login** (session persistence)

## 💡 Tips for Success

1. **Test on Physical Device** - Shake gesture works better on real device
2. **Prepare Firebase** - Set up before demo day
3. **Have Test Data** - Create sample exercises/routines beforehand
4. **Check Internet** - Firebase requires active connection
5. **Review Logs** - Monitor logcat during demo for any issues

## 📞 Support Resources

- **Firebase Documentation**: https://firebase.google.com/docs
- **Jetpack Compose Guide**: https://developer.android.com/jetpack/compose
- **Material Design 3**: https://m3.material.io/

---

**Ready to Run!** 🚀

If you've followed all steps, your FitLife app should be fully functional and ready for demonstration.
