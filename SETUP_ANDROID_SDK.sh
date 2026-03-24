#!/bin/bash

#================================================================
# Android SDK & Build Tools Installation Script
# For Order App Project
#================================================================

echo "📱 Android SDK Installation Setup"
echo "===================================="
echo ""

# Check if Java is available
if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    echo "✓ Java configured from Android Studio"
fi

# Set gradle path
GRADLE_BIN="$HOME/.gradle/wrapper/dists/gradle-8.2-bin/bbg7u40eoinfdyxsxr3z4i7ta/gradle-8.2/bin/gradle"

echo ""
echo "Step 1: Open Android Studio SDK Manager"
echo "==========================================="
echo ""
echo "The project requires the following Android packages:"
echo "  • Android SDK Platform 35 (API 35)"
echo "  • Android SDK Build Tools 34"
echo ""
echo "To install:"
echo "  1. Open Android Studio"
echo "  2. Go to: Tools → SDK Manager"
echo "  3. In SDK Platforms tab: Check 'Show Package Details'"
echo "  4. Install Android 15 (API 35)"
echo "  5. In SDK Tools tab: Install Build Tools 34.0.0"
echo "  6. Accept all license agreements"
echo "  7. Wait for installation to complete"
echo ""
echo "Alternatively, use command line (if sdkmanager is in PATH):"
echo "  sdkmanager \"build-tools;34.0.0\" \"platforms;android-35\""
echo "  sdkmanager --licenses"
echo ""

echo ""
echo "Step 2: After SDK Installation"
echo "================================="
echo ""
echo "Update local.properties with your SDK path:"
echo "  sdk.dir=/path/to/Android/sdk"
echo ""
echo "Common paths:"
echo "  • MacOS (Homebrew): /opt/homebrew/Cellar/android-sdk/35.0.0_1"
echo "  • MacOS (Standard): /Users/\$USER/Library/Android/sdk"
echo "  • Linux: /opt/android-sdk"
echo ""

echo ""
echo "Step 3: Build the APK"
echo "======================"
echo ""
echo "Once SDK is installed, run:"
echo "  export JAVA_HOME=/Applications/Android\\ Studio.app/Contents/jbr/Contents/Home"
echo "  cd /Users/coding/Desktop/trynchy"
echo "  ./gradlew clean build"
echo ""

echo ""
echo "Step 4: Find Your APK"
echo "====================="
echo ""
echo "After successful build:"
echo "  Debug APK: app/build/outputs/apk/debug/app-debug.apk"
echo "  Release APK: app/build/outputs/apk/release/app-release.apk"
echo ""

echo ""
echo "Need Help?"
echo "==========="
echo "Run the build command with --info flag for more details:"
echo "  ./gradlew build --info"
echo ""
