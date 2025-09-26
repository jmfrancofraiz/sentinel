#!/bin/bash

# Test script for Firebase Firestore integration
# This script helps verify that the WhatsApp Sentinel app is properly sending data to Firebase

echo "🔥 Testing Firebase Firestore Integration"
echo "========================================"

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK and add it to PATH"
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected. Please connect a device and enable USB debugging"
    exit 1
fi

echo "✅ Android device connected"

# Check if the app is installed
if ! adb shell pm list packages | grep -q "com.sentinel"; then
    echo "❌ WhatsApp Sentinel app not installed. Please install the app first:"
    echo "   ./gradlew assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk"
    exit 1
fi

echo "✅ WhatsApp Sentinel app is installed"

# Check if accessibility service is enabled
if ! adb shell settings get secure enabled_accessibility_services | grep -q "com.sentinel"; then
    echo "⚠️  Accessibility service not enabled. Please enable it in:"
    echo "   Settings > Accessibility > Downloaded apps > WhatsApp Sentinel"
    echo ""
    echo "Continuing with test anyway..."
fi

echo ""
echo "📱 Starting Firebase integration test..."
echo "========================================"
echo ""
echo "Instructions:"
echo "1. Open WhatsApp on your device"
echo "2. Navigate to any conversation (individual or group)"
echo "3. Wait for the app to detect and process the conversation"
echo "4. Check the logs below for Firebase-related messages"
echo ""
echo "Looking for these log messages:"
echo "✅ 'Firebase Authentication initialized'"
echo "✅ 'Authentication successful for user: ...'"
echo "✅ 'Firebase Firestore initialized successfully'"
echo "✅ 'Conversation data sent to Firebase Firestore'"
echo "✅ 'Conversation stored successfully with ID: ...'"
echo ""
echo "Press Ctrl+C to stop monitoring"
echo ""

# Monitor logs for Firebase-related messages
adb logcat -c  # Clear existing logs
adb logcat | grep -E "(FirebaseAuthService|FirebaseService|WhatsAppSentinel|Firebase|Firestore|Authentication)" --line-buffered
