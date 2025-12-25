# Google Maps Setup Guide

## Getting Your Google Maps API Key

To use the geotagging feature in FitLife, you need a Google Maps API key.

### Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Name it something like "FitLife App"

### Step 2: Enable Required APIs

1. In the Google Cloud Console, go to **APIs & Services > Library**
2. Search for and enable these APIs:
   - **Maps SDK for Android**
   - **Places API** (optional, for location search)
   - **Geocoding API** (optional, for address lookup)

### Step 3: Create API Key

1. Go to **APIs & Services > Credentials**
2. Click **Create Credentials > API Key**
3. Copy the API key that's generated

### Step 4: Restrict Your API Key (Recommended)

1. Click on your newly created API key
2. Under "Application restrictions", select **Android apps**
3. Click **Add an item** under "Restrict usage to your Android apps"
4. Add your app's package name: `com.natrajtechnology.fitfly`
5. Get your SHA-1 certificate fingerprint:
   ```powershell
   # For debug key (development)
   cd C:\Users\YourUsername\.android
   keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
6. Copy the SHA-1 fingerprint and add it
7. Under "API restrictions", select **Restrict key**
8. Select:
   - Maps SDK for Android
   - Places API (if enabled)
   - Geocoding API (if enabled)
9. Click **Save**

### Step 5: Add API Key to Your App

1. Open `app/src/main/AndroidManifest.xml`
2. Find this line:
   ```xml
   android:value="YOUR_GOOGLE_MAPS_API_KEY_HERE" />
   ```
3. Replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your actual API key

### Step 6: Build and Test

```powershell
cd "D:\Natraj Technology\Web Dev\fitness"
.\gradlew installDebug
```

## Features Enabled

✅ **Geotagging** - Tag exercises and routines with locations (gyms, yoga studios, parks)
✅ **Map View** - View all geotagged items on an interactive map
✅ **Current Location** - Use device GPS to get current coordinates
✅ **Location Types** - Categorize locations (GYM, YOGA_STUDIO, PARK, HOME, OTHER)
✅ **Interactive Markers** - Tap markers to view/edit exercises and routines

## Usage

1. **Add Location to Exercise/Routine**:
   - When creating or editing, click "Add Location Tag"
   - Enter location name (e.g., "Gold's Gym")
   - Select location type
   - Click "Use Current Location" to get GPS coordinates
   - Click Save

2. **View Map**:
   - From Dashboard, tap the location icon in the top bar
   - Grant location permission when prompted
   - See all geotagged exercises and routines as markers
   - Tap markers to navigate to that item

3. **Remove Location**:
   - Edit the exercise or routine
   - Click "Remove" on the location card

## Troubleshooting

**Map shows blank screen:**
- Verify API key is correct in AndroidManifest.xml
- Ensure Maps SDK for Android is enabled in Google Cloud Console
- Check that billing is enabled (Google requires it, but free tier is generous)

**Location permission denied:**
- Go to Settings > Apps > FitLife > Permissions
- Enable Location permission

**"Use Current Location" not working:**
- Make sure GPS is enabled on your device
- Grant location permission to the app
- Try moving outside if you're indoors

## API Usage Limits (Free Tier)

- **Maps SDK for Android**: 200,000 map loads per month (FREE)
- **Geocoding API**: 40,000 requests per month (FREE)
- **Places API**: 100,000 requests per month (FREE)

For a personal fitness app, you'll likely stay well within these limits!

## Privacy Note

Location data is stored in Firebase Firestore and linked to your user ID. 
Only you can see your geotagged exercises and routines.
