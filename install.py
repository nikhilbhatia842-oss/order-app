#!/usr/bin/env python3
"""Run this script to build and install the app on your connected Android device."""
import subprocess, os, glob, sys

JAVA_HOME = "/opt/homebrew/opt/openjdk@17"
PROJECT   = os.path.dirname(os.path.abspath(__file__))
ANDROID_HOME = os.path.expanduser("~/Library/Android/sdk")

gradle_bins = glob.glob(os.path.expanduser(
    "~/.gradle/wrapper/dists/gradle-8.6-bin/*/gradle-8.6/bin/gradle"))

if not gradle_bins:
    print("ERROR: Gradle 8.6 not found in cache. Open the project in Android Studio once to download it.")
    sys.exit(1)

gradle_bin = gradle_bins[0]

env = os.environ.copy()
env["JAVA_HOME"]    = JAVA_HOME
env["PATH"]         = JAVA_HOME + "/bin:" + env.get("PATH", "")
env["ANDROID_HOME"] = ANDROID_HOME

print("Building and installing app on connected device...")
print("(Make sure your phone is connected via USB with USB Debugging enabled)\n")

r = subprocess.run([gradle_bin, "installDebug"], cwd=PROJECT, env=env)

if r.returncode == 0:
    print("\nSUCCESS! App installed. Look for 'Order App' on your device.")
else:
    print("\nBuild failed. Check the output above for errors.")

sys.exit(r.returncode)
