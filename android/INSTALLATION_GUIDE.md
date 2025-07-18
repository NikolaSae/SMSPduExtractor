# Installation Guide - SMS PDU Analyzer Android App

## Option 1: Build from Source (Recommended)

### Prerequisites
- Android Studio (latest version)
- Android SDK (API 24 or higher)
- Java 8 or higher

### Steps
1. **Download the project**:
   - Clone or download the `android` folder from this project

2. **Open in Android Studio**:
   - File → Open → Select the `android` folder
   - Wait for Gradle sync to complete

3. **Build the APK**:
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or use terminal: `./gradlew assembleDebug`

4. **Find the APK**:
   - Location: `android/app/build/outputs/apk/debug/app-debug.apk`

5. **Install on device**:
   - Enable "Unknown sources" in Android settings
   - Transfer APK to device and install

## Option 2: Command Line Build

### Prerequisites
- Android SDK installed
- ANDROID_HOME environment variable set

### Commands
```bash
cd android
./gradlew assembleDebug
```

The APK will be created at: `app/build/outputs/apk/debug/app-debug.apk`

## Installation on Android Device

1. **Enable Unknown Sources**:
   - Settings → Security → Unknown Sources (enable)
   - Or Settings → Apps → Special Access → Install Unknown Apps

2. **Transfer APK**:
   - Copy `app-debug.apk` to your device
   - Via USB, email, or cloud storage

3. **Install**:
   - Open file manager on device
   - Navigate to APK location
   - Tap to install
   - Grant permissions when prompted

## Permissions Required

The app will request these permissions:
- **SMS permissions**: To automatically capture SMS PDU data
- **Storage permissions**: To save analysis data locally

## First Run

1. Launch "SMS PDU Analyzer"
2. Grant SMS permissions when prompted
3. Send an SMS to your device to test automatic analysis
4. Or use the + button to manually analyze PDU strings

## Troubleshooting

- **Build fails**: Check Android SDK version and Gradle sync
- **App crashes**: Ensure Android 7.0+ and permissions granted
- **No SMS captured**: Check SMS permissions and try manual analysis first

## Testing

Use these sample PDU strings to test:
- `0791448720003023240DD0E474747A8C07C9D2E03D4BF7399FE6`
- `0011000B919471476965870000080048656C6C6F20576F726C6421`