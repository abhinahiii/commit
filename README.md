# Commit

I bookmark a lot of articles and videos but I forget to watch or read them.

So, I built an app that lets me share articles or videos directly into it, and it automatically creates a Google Calendar event so I stay reminded.

## Features

- **Share to Commit** — Share any link from any app, pick a time, and it's on your calendar
- **Smart Home Screen** — See your upcoming items, what's overdue, and a friendly greeting
- **Event Management** — Mark items as done, reschedule, or archive them
- **Calendar Sync** — Stays in sync with Google Calendar, so if you delete an event there, it updates here
- **Premium Design** — Clean, "Paper" interface with noise textures and serif typography

## how it works

1. share a link to readlater from any app (browser, twitter, youtube, etc.)
2. pick when you want to read/watch it
3. readlater creates a calendar event with the link
4. when the time comes, your calendar reminds you

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
5. share any link to readlater from any app

## project structure

```
readlater/
├── app/src/main/java/com/readlater/
│   ├── MainActivity.kt              # home screen host
│   ├── ShareActivity.kt             # share overlay
│   ├── ReadLaterApp.kt              # application class
│   ├── ui/
│   │   ├── theme/Theme.kt           # light theme, clean typography
│   │   ├── components/
│   │   │   ├── EventCard.kt         # event display cards
│   │   │   ├── MetroButton.kt       # styled buttons
│   │   │   ├── RescheduleDialog.kt  # date/time picker dialog
│   │   │   └── ConfirmationDialogs.kt
│   │   └── screens/
│   │       ├── HomeScreen.kt        # main screen with tabs
│   │       └── ShareOverlayScreen.kt
│   ├── data/
│   │   ├── AuthRepository.kt        # google sign-in
│   │   ├── CalendarRepository.kt    # google calendar api
│   │   ├── EventRepository.kt       # local + remote sync
│   │   ├── SavedEvent.kt            # event entity
│   │   ├── SavedEventDao.kt         # room dao
│   │   └── AppDatabase.kt           # room database
│   └── util/
│       └── UrlMetadataFetcher.kt    # url title fetching
└── app/src/main/AndroidManifest.xml
```

## tech stack

- kotlin + jetpack compose
- room database (local event storage)
- google sign-in sdk
- google calendar api
- jsoup (html parsing for link titles)
- material 3

## screens

### onboarding
connect your google calendar to get started.

### home
- **upcoming** — events you've scheduled, sorted by date
- **done** — items you've marked as complete
- **archived** — items you've archived (can restore or delete)

### share overlay
appears when you share a link. pick title, date, time, and duration.

## license

mit
=======
# commit
>>>>>>> b34daca33dbb3e431852bab67348212a143c6295
