# readlater

i bookmark a lot of articles and videos but i forget to watch or read them.

so, i am designing an app that lets me share articles or videos directly into it, and it automatically creates a google calendar event so i stay reminded.

## setup

### 1. google cloud console setup

1. go to [google cloud console](https://console.cloud.google.com/)
2. create a new project or select an existing one
3. enable the **google calendar api**:
   - go to apis & services > library
   - search for "google calendar api"
   - click enable

4. configure oauth consent screen:
   - go to apis & services > oauth consent screen
   - select "external" user type
   - fill in app name: "readlater"
   - add your email as developer contact
   - add scope: `https://www.googleapis.com/auth/calendar.events`
   - add your email as a test user (required while in testing mode)

5. create oauth 2.0 credentials:
   - go to apis & services > credentials
   - click "create credentials" > oauth client id
   - select "android" as application type
   - package name: `com.readlater`
   - get your sha-1 fingerprint:
     ```bash
     # for debug builds
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - enter the sha-1 fingerprint
   - click create

### 2. build the app

```bash
cd readlater
./gradlew assembleDebug
```

the apk will be at `app/build/outputs/apk/debug/app-debug.apk`

### 3. install and use

1. install the apk on your android device
2. open readlater and tap "connect google calendar"
3. sign in with your google account
4. grant calendar permissions
5. now share any link to readlater from any app

## project structure

```
readlater/
├── app/src/main/java/com/readlater/
│   ├── MainActivity.kt          # setup screen
│   ├── ShareActivity.kt         # share overlay
│   ├── ReadLaterApp.kt          # application class
│   ├── ui/
│   │   ├── theme/Theme.kt       # metro theme
│   │   ├── components/          # reusable ui components
│   │   └── screens/             # screen composables
│   ├── data/
│   │   ├── AuthRepository.kt    # google auth
│   │   └── CalendarRepository.kt # calendar api
│   └── util/
│       └── UrlMetadataFetcher.kt # url title fetching
└── app/src/main/AndroidManifest.xml
```

## tech stack

- kotlin + jetpack compose
- google sign-in sdk
- google calendar api
- jsoup (html parsing)
- material 3 (metro style)
