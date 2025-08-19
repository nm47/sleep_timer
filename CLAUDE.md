# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

- **Build debug APK**: `./gradlew assembleDebug`
- **Run tests**: `./gradlew test`
- **Run lint checks**: `./gradlew lint`
- **Full CI build**: `./gradlew assembleDebug test lint --stacktrace`

## Project Architecture

This is a minimal Android sleep timer app that provides only a Quick Settings Tile (no launcher activity). The app consists of three main components:

### Core Components

1. **SleepTileService** (`app/src/main/java/fr/smarquis/sleeptimer/SleepTileService.kt`):
   - Extends `TileService` to provide Quick Settings Tile functionality
   - Handles tile clicks to toggle sleep timer
   - Updates tile state based on active notifications
   - Requests notification permissions when needed

2. **SleepNotification** (`app/src/main/java/fr/smarquis/sleeptimer/SleepNotification.kt`):
   - Object singleton managing notification lifecycle
   - Creates countdown notifications with increment/decrement/cancel actions
   - Default 30-minute timer with 10-minute increment/decrement steps
   - Uses `setTimeoutAfter()` for automatic dismissal

3. **SleepAudioService** (`app/src/main/java/fr/smarquis/sleeptimer/SleepAudioService.kt`):
   - `IntentService` that handles audio fade-out when timer expires
   - Gradually lowers media volume over 1-second intervals
   - Requests audio focus to pause current media playback
   - Restores original volume after 2 seconds

### App Configuration

- **Package**: `fr.smarquis.sleeptimer`
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35
- **Java version**: 11
- **Kotlin version**: 2.2.10
- **Build variants**: Debug uses custom keystore (`debug.keystore`), Release enables minification

### Key Android APIs Used

- Quick Settings Tile API (`TileService`)
- Notification timeout API (`setTimeoutAfter`)
- Audio focus management (`AudioManager`)
- Media volume control (`adjustStreamVolume`, `setStreamVolume`)

### Testing and CI

The CI pipeline runs `assembleDebug test lint` on every push/PR. Test reports and lint results are uploaded as artifacts.
- @flash.sh will build and flash the app to the connected device