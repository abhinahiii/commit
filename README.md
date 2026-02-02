# ReadLater

Schedule time for content you discover. Share any link to create a Google Calendar event.

## Setup

### 1. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Google Calendar API**:
   - Go to APIs & Services > Library
   - Search for "Google Calendar API"
   - Click Enable

4. Configure OAuth Consent Screen:
   - Go to APIs & Services > OAuth consent screen
   - Select "External" user type
   - Fill in app name: "ReadLater"
   - Add your email as developer contact
   - Add scope: `https://www.googleapis.com/auth/calendar.events`
   - Add your email as a test user (required while in testing mode)

5. Create OAuth 2.0 Credentials:
   - Go to APIs & Services > Credentials
   - Click "Create Credentials" > OAuth client ID
   - Select "Android" as application type
   - Package name: `com.readlater`
   - Get your SHA-1 fingerprint:
     ```bash
     # For debug builds
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - Enter the SHA-1 fingerprint
   - Click Create

### 2. Build the App

```bash
cd readlater
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### 3. Install and Use

1. Install the APK on your Android device
2. Open ReadLater and tap "Connect Google Calendar"
3. Sign in with your Google account
4. Grant calendar permissions
5. Now share any link to ReadLater from any app

## Project Structure

```
readlater/
├── app/src/main/java/com/readlater/
│   ├── MainActivity.kt          # Setup screen
│   ├── ShareActivity.kt         # Share overlay
│   ├── ReadLaterApp.kt          # Application class
│   ├── ui/
│   │   ├── theme/Theme.kt       # Brutalist theme
│   │   ├── components/          # Reusable UI components
│   │   └── screens/             # Screen composables
│   ├── data/
│   │   ├── AuthRepository.kt    # Google auth
│   │   └── CalendarRepository.kt # Calendar API
│   └── util/
│       └── UrlMetadataFetcher.kt # URL title fetching
└── app/src/main/AndroidManifest.xml
```

## Tech Stack

- Kotlin + Jetpack Compose
- Google Sign-In SDK
- Google Calendar API
- Jsoup (HTML parsing)
- Material 3 (styled brutalist)
