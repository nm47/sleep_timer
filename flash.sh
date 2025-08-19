#!/usr/bin/env bash
set -e

# Flash Sleep Timer app to connected Android device
# Usage: ./flash.sh

echo "🔧 Setting up Android SDK environment..."
source scripts/env.sh

echo "📱 Checking ADB connection..."
adb devices

echo "🔨 Building debug APK..."
./gradlew assembleDebug

echo "📦 Installing APK to device..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

echo "✅ Sleep Timer app successfully installed!"
echo "💡 Add the tile to Quick Settings to use the app"