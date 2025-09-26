#!/bin/bash

# Sentinel - Install and Test Script
# This script helps install and test the model on your phone

echo "🚀 Sentinel - Model Test"
echo "================================="

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK and add it to PATH"
    echo "   Download from: https://developer.android.com/studio"
    exit 1
fi

# Check if device is connected
echo "📱 Checking for connected devices..."
adb devices

if [ $(adb devices | grep -c "device$") -eq 0 ]; then
    echo "❌ No devices connected. Please:"
    echo "   1. Enable USB Debugging on your phone"
    echo "   2. Connect your phone via USB"
    echo "   3. Allow USB debugging when prompted"
    exit 1
fi

echo "✅ Device found!"

# Install the APK
echo "📦 Installing WhatsApp Sentinel..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -eq 0 ]; then
    echo "✅ APK installed successfully!"
else
    echo "❌ Failed to install APK"
    exit 1
fi

echo ""
echo "🧪 Testing the Model..."
echo "======================"

# Launch the test activity
echo "🚀 Launching Model Test Activity..."
adb shell am start -n com.whatsappsentinel.debug/.ModelTestActivity

echo ""
echo "📋 What to do next:"
echo "==================="
echo "1. The Model Test Activity should open on your phone"
echo "2. It will automatically test the model initialization"
echo "3. Check the screen for results:"
echo "   - ✅ 'Model initialized successfully!' = Model works"
echo "   - ❌ 'Model test failed: ...' = Check the error message"
echo ""
echo "4. To test the accessibility service:"
echo "   - Go to Settings > Accessibility > Downloaded apps"
echo "   - Find 'WhatsApp Sentinel' and enable it"
echo "   - Open WhatsApp and navigate to a chat"
echo "   - The service should monitor and log contact names"
echo ""
echo "5. To view logs:"
echo "   adb logcat | grep WhatsAppSentinel"
echo ""
echo "6. To uninstall:"
echo "   adb uninstall com.whatsappsentinel.debug"
echo ""

# Show logcat in background
echo "📊 Starting log monitoring (press Ctrl+C to stop)..."
echo "   Look for 'ModelManager' and 'InferenceEngine' logs"
echo ""
adb logcat | grep -E "(WhatsAppSentinel|ModelManager|InferenceEngine)"
