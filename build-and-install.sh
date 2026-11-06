#!/usr/bin/env bash
#
# Builds Eva Cards Pack Release APK and installs it to an Android TV via ADB.
#
# Usage:
#   ./build-and-install.sh [192.168.10.33:5555]

set -e

IP="${1:-}"

echo "🔨 Building Release APK using Gradle..."
./gradlew app:assembleRelease

APK_PATH="app/build/outputs/apk/release/app-release.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "❌ Release APK build failed! File not found at $APK_PATH"
    exit 1
fi

echo "✅ Release APK built successfully at $APK_PATH"

if [ -n "$IP" ]; then
    echo "🔌 Connecting to Android TV at $IP..."
    adb connect "$IP"
    echo "📲 Installing APK..."
    adb -s "$IP" install -r "$APK_PATH"
    echo "🚀 Launching Eva Cards Pack Activity..."
    adb -s "$IP" shell am start -n io.rns.evacardspack/.EvaCardActivity
else
    echo "📲 Installing APK..."
    adb install -r "$APK_PATH"
    echo "🚀 Launching Eva Cards Pack Activity..."
    adb shell am start -n io.rns.evacardspack/.EvaCardActivity
fi

echo "🎉 Done!"
