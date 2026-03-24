# 🚀 Complete Android SDK Setup & Build Guide

## Current Status

✅ **Completed:**
- Java 21 (from Android Studio JBR)
- Gradle 8.2
- Project structure & code
- Dependencies configured

❌ **Needed:**
- Android SDK (Build Tools & Platform SDK)

---

## Why it's needed

The Android SDK provides the official libraries and tools to compile Android applications:
- **Build Tools 34**: Compiles Java code to Android bytecode
- **Platform 35 (API 35)**: Android 15 framework libraries

---

## Installation Steps

### Step 1: Open Android Studio SDK Manager

  **Path:** Android Studio → Preferences → Appearance & Behavior → System Settings → Android SDK

  OR direct path on macOS:
  ```
  /Applications/Android Studio.app/Contents/bin/studio
  ```

### Step 2: Install Required SDK Packages

  In the **SDK Platforms** tab:
  - ☑ Check "Show Package Details"
  - ☑ Android 15 (API 35)

  In the **SDK Tools** tab:
  - ☑ Android SDK Build-Tools 34
  - ☑ Accept all License Agreements

  Click **Apply** and wait for download to complete (~500MB)

### Step 3: Find Your SDK Location

  After installation, the SDK will typically be at one of these locations:

  **For macOS (M1/M2/M3 Macs):**
  ```
  /opt/homebrew/Cellar/android-sdk/35.0.0_1/
  ```
  OR (if installed via Android Studio)
  ```
  /Users/YOUR_USERNAME/Library/Android/sdk
  ```

  **For Intel Macs:**
  ```
  /Users/YOUR_USERNAME/Library/Android/sdk
  ```

  **For Linux:**
  ```
  /opt/android-sdk
  ```

### Step 4: Update local.properties

  Edit `/Users/coding/Desktop/trynchy/local.properties` and set your SDK path:

  ```properties
  sdk.dir=/opt/homebrew/Cellar/android-sdk/35.0.0_1
  ```
  
  OR

  ```properties
  sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
  ```

  Replace `YOUR_USERNAME` with your actual macOS username.

### Step 5: Build the APK

  Open Terminal and run:

  ```bash
  cd /Users/coding/Desktop/trynchy

  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"

  ./gradlew clean build
  ```

  This will:
  - Clean previous builds
  - Download dependencies (Retrofit, Gson, Material Design)
  - Compile Java source code
  - Build Debug and Release APKs

---

## Expected Output

If successful, you should see:

```
BUILD SUCCESSFUL in XXs

Generated APK files:
  • app/build/outputs/apk/debug/app-debug.apk
  • app/build/outputs/apk/release/app-release.apk (requires signing)
```

---

## Your APK Locations

After successful build:

**Debug APK:** (Ready to install for testing)
```
/Users/coding/Desktop/trynchy/app/build/outputs/apk/debug/app-debug.apk
```

**Release APK:** (For Google Play Store - requires signing)
```
/Users/coding/Desktop/trynchy/app/build/outputs/apk/release/app-release.apk
```

---

## ❓ Troubleshooting

| Problem | Solution |
|---------|----------|
| "SDK location not found" | Check Step 3 & 4 - ensure `local.properties` has correct SDK path |
| "Could not find gradle-wrapper.jar" | Run `./gradlew --version` first to initialize wrapper |
| Build hangs | Press Ctrl+C and try again with `--info` flag for more details |
| Java not found | Ensure `JAVA_HOME` is set to Android Studio's JBR |
| Cannot open Android Studio | System might need Java or Xcode CLT updates |

---

## ⚡ Quick Reference Commands

```bash
# Set Java path (one-time per terminal session)
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Verify Java is working
java -version

# Navigate to project
cd /Users/coding/Desktop/trynchy

# Build APK
./gradlew clean build

# Build only debug APK (faster)
./gradlew assembleDebug

# Build with detailed output
./gradlew build --info

# Clean up build artifacts
./gradlew clean

# Check what tasks are available
./gradlew tasks

# Install and run on device/emulator
./gradlew installDebug
```

---

## 📦 Installing the APK

Once you have the APK file:

### On Android Device:
1. Connect device via USB
2. Enable USB Debugging (Settings → Developer Options)
3. Run: `./gradlew installDebug`

### Using adb (Android Debug Bridge):
```bash
# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Install and launch
adb shell am start -n com.orderapp/.MainActivity
```

### Manually:
1. Copy `app-debug.apk` to device
2. Open File Manager on device
3. Tap APK file to install
4. Grant permissions if prompted

---

## 🎯 Next Steps After Build

1. **Update Telegram Credentials**
   - Get Bot Token from @BotFather on Telegram
   - Get Chat ID from your bot's getUpdates API
   - Update in: `app/src/main/java/com/orderapp/api/TelegramBotAPIClient.java`
   - Rebuild with: `./gradlew clean build`

2. **Test the App**
   - Install on Android device/emulator
   - Fill sample order form
   - Click Submit
   - Verify message appears in Telegram

3. **Release Build** (for Google Play Store)
   - Create keystore: `keytool -genkey -v -keystore order-app.keystore -keyalg RSA -keysize 2048`
   - Sign APK in Android Studio
   - Upload to Google Play Console

---

**Questions?** Refer to [README.md](README.md) or Android documentation.
