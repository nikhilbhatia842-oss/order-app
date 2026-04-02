#!/usr/bin/env python3
"""Run this script to build and install the RELEASE app on your connected Android device."""
import subprocess, os, glob, sys

JAVA_HOME = "/opt/homebrew/opt/openjdk@17"
PROJECT   = os.path.dirname(os.path.abspath(__file__))
ANDROID_HOME = os.path.expanduser("~/Library/Android/sdk")

# Search for any cached gradle distribution (8.6, 8.9, etc.)
gradle_bins = glob.glob(os.path.expanduser(
    "~/.gradle/wrapper/dists/gradle-*/*/gradle-*/bin/gradle"))

if not gradle_bins:
    print("ERROR: No Gradle installation found in cache. Open the project in Android Studio once to download it.")
    sys.exit(1)

# Prefer the highest version available
gradle_bin = sorted(gradle_bins)[-1]
print(f"Using Gradle: {gradle_bin}")

env = os.environ.copy()
env["JAVA_HOME"]    = JAVA_HOME
env["PATH"]         = JAVA_HOME + "/bin:" + env.get("PATH", "")
env["ANDROID_HOME"] = ANDROID_HOME

print("Building release APK (signed with .envKey/release.keystore)...")
print("(Make sure your phone is connected via USB with USB Debugging enabled)\n")

# Uninstall existing app first to avoid signature mismatch errors
print("Uninstalling existing app (if any)...")
subprocess.run(["adb", "uninstall", "com.orderapp"], env=env, capture_output=True)

r = subprocess.run([gradle_bin, "installRelease"], cwd=PROJECT, env=env)

if r.returncode == 0:
    print("\nSUCCESS! Release app installed. Look for 'Order App' on your device.")
else:
    print("\nBuild failed. Check the output above for errors.")

sys.exit(r.returncode)
