# FitLife - Fitness Tracking Android App

A comprehensive fitness tracking Android application built with Kotlin, Jetpack Compose, and Firebase for a university assessment.

## 🎯 Project Overview

FitLife is a modern Android application designed to help busy professionals plan workouts, manage exercises, track completion, and delegate workout checklists via SMS. The app demonstrates clean architecture principles using MVVM pattern with Material Design 3.

## ✨ Key Features

### 1️⃣ **Home Screen (6 Marks)**
- ✅ Professional landing page with app logo and tagline
- ✅ Login and Sign Up buttons with Material Design
- ✅ Smooth navigation to authentication screens

### 2️⃣ **User Authentication (8 Marks)**
- ✅ Firebase Authentication integration
- ✅ Email + Password registration
- ✅ Secure login with session persistence
- ✅ Input validation and error handling
- ✅ Auto-login for authenticated users

### 3️⃣ **Exercise & Routine Management**

#### A. Delete Items (4 Marks)
- ✅ Delete exercises and routines
- ✅ Confirmation dialogs before deletion
- ✅ Real-time Firestore sync

#### B. Edit Items (8 Marks)
- ✅ Edit exercise details (name, sets, reps, instructions, equipment)
- ✅ Edit routine details (name, description, exercises, equipment)
- ✅ Immediate UI and database updates

#### C. Mark as Completed (4 Marks)
- ✅ Toggle completion status with checkboxes
- ✅ Visual distinction (strikethrough, faded colors)
- ✅ Persistent status in Firestore

### 4️⃣ **Create Workout Routines (8 Marks)**
- ✅ Create routines with multiple exercises
- ✅ Assign equipment requirements
- ✅ Add instructions and descriptions
- ✅ Auto-generate equipment checklists
- ✅ Save to Firestore with user association

### 5️⃣ **SMS Delegation (10 Marks)**
- ✅ Select routines or equipment checklists
- ✅ Enter phone number manually
- ✅ Auto-generate formatted SMS message
- ✅ Include workout name, exercises, equipment list
- ✅ Optional gym notes
- ✅ Pre-filled SMS app (safe, no auto-send)

### 6️⃣ **Gesture Controls - DESIRABLE FEATURE (12 Marks)**
- ✅ **Swipe Left** → Delete exercise/routine
- ✅ **Swipe Right** → Mark as completed
- ✅ **Shake Device** → Reset all workout checklists (with confirmation)
- ✅ Uses ItemTouchHelper for swipes
- ✅ Uses SensorManager for shake detection

## 🏗️ Architecture

### MVVM (Model-View-ViewModel)
```
├── data/
│   ├── model/          # Data classes (Exercise, WorkoutRoutine, User)
│   └── repository/     # Data layer (AuthRepository, ExerciseRepository, RoutineRepository)
├── ui/
│   ├── screens/        # Composable screens
│   ├── theme/          # Material Design theme
│   └── viewmodel/      # ViewModels for business logic
└── navigation/         # Navigation graph
```

## 🔧 Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM
- **Backend**: Firebase (Authentication + Firestore)
- **Navigation**: Jetpack Navigation Compose
- **Async**: Kotlin Coroutines + Flow
- **Design**: Material Design 3
- **Build System**: Gradle (Kotlin DSL)

## 📦 Dependencies

```kotlin
// Core Android
- androidx.core:core-ktx:1.16.0
- androidx.lifecycle:lifecycle-runtime-ktx:2.9.2
- androidx.activity:activity-compose:1.10.1

// Jetpack Compose
- androidx.compose:compose-bom:2024.04.01
- androidx.compose.material3
- androidx.compose.ui

// Firebase
- com.google.firebase:firebase-bom:33.7.0
- firebase-auth-ktx
- firebase-firestore-ktx

// Navigation
- androidx.navigation:navigation-compose:2.8.5

// Lifecycle
- androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2

// Image Loading
- io.coil-kt:coil-compose:2.5.0
```

## 🚀 Getting Started

### Prerequisites
1. Android Studio (latest version)
2. Android SDK (API 24+)
3. Firebase project setup

### Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing one
3. Add Android app with package name: `com.natrajtechnology.fitfly`
4. Download `google-services.json` and replace the placeholder file in `app/` directory
5. Enable Authentication (Email/Password) in Firebase Console
6. Enable Firestore Database in Firebase Console

### Installation
1. Clone the repository
2. Open project in Android Studio
3. Replace `google-services.json` with your Firebase configuration
4. Sync Gradle files
5. Run on emulator or physical device

```bash
# Build the project
./gradlew build

# Install on device
./gradlew installDebug
```

## 📱 App Screens

### Authentication Flow
1. **Home Screen** - Landing page with login/signup options
2. **Sign Up Screen** - New user registration
3. **Login Screen** - Existing user authentication

### Main Application
4. **Dashboard** - Tabbed view (Exercises | Routines)
5. **Add/Edit Exercise** - CRUD operations for exercises
6. **Add/Edit Routine** - Create workout routines
7. **Routine Detail** - View routine with SMS delegation

## 🎨 Design Features

- **Material Design 3** components
- **Dynamic color theming**
- **Responsive layouts**
- **Smooth animations**
- **Gesture-based interactions**

## 🔐 Security Features

- Firebase Authentication for secure login
- User-specific data isolation in Firestore
- Input validation on all forms
- Password visibility toggle
- Session management with auto-login

## 📊 Database Structure

### Firestore Collections

**exercises**
```json
{
  "id": "auto-generated",
  "name": "Push-ups",
  "sets": 3,
  "reps": 15,
  "instructions": "Keep back straight",
  "requiredEquipment": ["Mat"],
  "isCompleted": false,
  "userId": "user-uid",
  "createdAt": 1234567890
}
```

**routines**
```json
{
  "id": "auto-generated",
  "name": "Morning Workout",
  "description": "Quick morning routine",
  "exerciseIds": ["ex1", "ex2"],
  "requiredEquipment": ["Mat", "Dumbbells"],
  "isCompleted": false,
  "userId": "user-uid",
  "createdAt": 1234567890
}
```

## 🧪 Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## 📝 User Story Implementation

✅ **Scenario Supported:**
1. User registers and logs in
2. Browses workout routines
3. Creates a weekly plan
4. Reviews equipment checklist
5. Sends checklist via SMS to a friend
6. Marks exercises as done
7. Uses gesture controls for quick actions

## 🎯 Assessment Criteria Coverage

| Feature | Marks | Status |
|---------|-------|--------|
| Home Screen | 6 | ✅ Complete |
| User Authentication | 8 | ✅ Complete |
| Delete Items | 4 | ✅ Complete |
| Edit Items | 8 | ✅ Complete |
| Mark as Completed | 4 | ✅ Complete |
| Create Routines | 8 | ✅ Complete |
| SMS Delegation | 10 | ✅ Complete |
| Gesture Controls | 12 | ✅ Complete |
| **Total** | **60** | **✅ 100%** |

## 🔥 Advanced Features

- **Real-time sync** with Firebase Firestore
- **Offline support** (Firestore caching)
- **Shake detection** using accelerometer
- **Swipe gestures** for quick actions
- **Auto-equipment generation** from exercises
- **Formatted SMS messages** with emojis
- **Material Design 3** with dynamic theming

## 📄 Permissions Required

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
```

## 🐛 Known Issues & Future Enhancements

- [ ] Add image upload for exercises (placeholder implemented)
- [ ] Implement workout history/analytics
- [ ] Add dark mode toggle
- [ ] Export routines as PDF
- [ ] Social sharing features
- [ ] Workout reminders/notifications

## 👨‍💻 Developer Information

**Project Name**: FitLife
**Package**: com.natrajtechnology.fitfly
**Min SDK**: 24 (Android 7.0)
**Target SDK**: 35 (Android 15)
**Compile SDK**: 35

## 📞 Support

For issues or questions:
- Check Firebase configuration
- Ensure all permissions are granted
- Verify internet connection for Firebase operations
- Check logcat for detailed error messages

## 📜 License

This project is created for educational purposes as part of a university assessment.

---

**Built with ❤️ using Kotlin & Jetpack Compose**
