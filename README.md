# Vora

A clean, minimal music player for Android. Play your local library, download songs and full albums directly from YouTube, and manage everything from one place.

> **Download the latest APK** → [Releases](https://github.com/peedrovzxf/vora-app/releases)

---

## Screenshots

<p align="center">
  <img src="screenshots/Screenshot_20260323_031710.jpg" width="180"/>
  <img src="screenshots/Screenshot_20260323_031715.jpg" width="180"/>
  <img src="screenshots/Screenshot_20260323_031720.jpg" width="180"/>
  <img src="screenshots/Screenshot_20260323_031724.jpg" width="180"/>
  <img src="screenshots/Screenshot_20260323_031728.jpg" width="180"/>
  <img src="screenshots/Screenshot_20260323_031730.jpg" width="180"/>
</p>

---

## Features

- **Local library** — browse and play music from your device via MediaStore
- **Albums & playlists** — full library organization with custom artwork, artist, and title editing
- **YouTube downloads** — search and download individual songs or full playlists/albums directly from within the app
- **Playback controls** — persistent player with play/pause, skip, shuffle, and queue management
- **Custom metadata** — edit album art, artist name, and album title per album

---

## Installation

1. Go to [Releases](https://github.com/peedrovzxf/vora-app/releases)
2. Download the latest `vora-vX.X.X.apk`
3. On your Android device, open the APK and tap **Install**
4. If prompted, enable **Install from unknown sources** in your device settings

> Requires Android 8.0 (API 26) or higher.

---

## YouTube API Key Setup

Vora uses the YouTube Data API v3 to search and download music. You need to provide your own API key.

### 1. Create a Google Cloud project

1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Click **Select a project → New Project**
3. Give it a name (e.g. `vora`) and click **Create**

### 2. Enable the YouTube Data API

1. In the sidebar go to **APIs & Services → Library**
2. Search for **YouTube Data API v3**
3. Click it and press **Enable**

### 3. Create an API key

1. Go to **APIs & Services → Credentials**
2. Click **Create Credentials → API key**
3. Copy the generated key
4. (Optional but recommended) Click **Restrict key** → under API restrictions select **YouTube Data API v3**

### 4. Add the key to Vora

1. Open the app → go to the **Downloads** screen
2. Paste your API key when prompted

> The free tier includes 10,000 units/day which is more than enough for personal use.

---

## Build from Source

```bash
git clone https://github.com/peedrovzxf/vora-app.git
cd vora
```

1. Open the project in **Android Studio Hedgehog** or later
2. Run on a device or emulator with **API 26+**

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Database | Room |
| Playback | ExoPlayer |
| Local media | MediaStore |
| YouTube | YouTube Data API v3 |
| Image loading | Coil |

---

## Roadmap

- [ ] Equalizer
- [ ] Theme / color customization
