# Commit - Android

> **Bookmarks are intentions. Commitments are actions.**

We save countless articles, videos, and threads, promising ourselves we'll get to them "later." But later rarely comes. The backlog grows, and so does the guilt.

**Commit** is different. It’s not a bookmark manager - it’s an action scheduler. When you share content to Commit, you don't just save it; you promise a time to engage with it.

---

## Core Features

### 1. Intentional Scheduling
Share any link from any app directly to Commit. Instead of a passively saving it to a list you'll never read, you are prompted to **Commit** to a time. Choose **Tomorrow Morning**, **This Weekend**, or a specific date and time.

### 2. Google Calendar Sync
Commitments aren't real unless they are on your calendar. The app instantly syncs your scheduled reading or watching sessions to your Google Calendar, complete with the link and duration.

### 3. Premium "Paper" Design
A visual departure from standard apps. Commit features a tactile **Paper** aesthetic with noise textures, diffused shadows, and editorial serif typography. It feels grounded, calm, and focuses entirely on the content.

---

## How It Works

1.  **Share**: Find an article or video in Chrome, YouTube, or Twitter. Tap "Share" and select **Commit**.
2.  **Schedule**: A beautiful overlay slides up. Pick a time and duration (e.g., 15m, 30m).
3.  **Sync**: The event is added to your Google Calendar.
4.  **Consume**: When the time comes, open the link directly from your calendar.

---

## Setup & Build

### Prerequisites
-   Android Studio Koala or newer
-   JDK 17
-   Google Cloud Console Project (for Calendar API)

### Google Cloud Setup
1.  Create a project in [Google Cloud Console](https://console.cloud.google.com/).
2.  Enable **Google Calendar API**.
3.  Create OAuth 2.0 Credentials (Android).
    -   Package name: `com.readlater`
    -   Add your SHA-1 fingerprint (debug/release).
4.  Add your email as a Test User in the OAuth Consent Screen.

### Build
```bash
git clone https://github.com/abhinahiii/commit.git
cd commit-android
./gradlew assembleDebug
```

---

## Tech Stack

-   **Language**: Kotlin
-   **UI**: Jetpack Compose (Material 3 heavily customized)
-   **Architecture**: MVVM
-   **Remote Sync**: Google Calendar API v3
-   **Auth**: Google Sign-In
-   **Parsing**: Jsoup (for link metadata)

---

## License

MIT. Go build something inevitable.
