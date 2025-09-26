# Firebase Firestore Integration Setup

This guide will help you set up Firebase Firestore integration for the WhatsApp Sentinel project to store conversation data in a "whatsapp" collection.

## Prerequisites

1. A Google account
2. Android Studio with Firebase support
3. The WhatsApp Sentinel project already set up

## Step 1: Create Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" or "Add project"
3. Enter project name (e.g., "whatsapp-sentinel")
4. Choose whether to enable Google Analytics (optional)
5. Click "Create project"

## Step 2: Add Android App to Firebase

1. In your Firebase project, click "Add app" and select Android
2. Enter your package name: `com.sentinel`
3. Enter app nickname: "WhatsApp Sentinel"
4. Enter SHA-1 certificate fingerprint (optional for debug builds)
5. Click "Register app"

## Step 3: Download Configuration File

1. Download the `google-services.json` file
2. Replace the template file at `app/google-services.json.template` with your actual `google-services.json`
3. Make sure the file is placed at `app/google-services.json` (not in the template location)

## Step 4: Enable Firestore

1. In your Firebase project, go to "Firestore Database"
2. Click "Create database"
3. Choose "Start in test mode" for development (you can secure it later)
4. Select a location for your database
5. Click "Done"

## Step 5: Configure Firestore Security Rules (Optional)

For development, you can use these permissive rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**⚠️ Warning**: These rules allow anyone to read/write your data. For production, implement proper authentication and security rules.

## Step 6: Build and Test

1. Clean and rebuild your project:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

2. Install the APK on your device:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. Enable the accessibility service in Settings > Accessibility > Downloaded apps

4. Open WhatsApp and navigate to a conversation

5. Check the logs to see Firebase integration:
   ```bash
   adb logcat | grep -E "(FirebaseService|WhatsAppSentinel)"
   ```

## Data Structure

The conversation data will be stored in Firestore with the following structure:

### Collection: `whatsapp`

Each document contains:

```json
{
  "timestamp": "15/12/2024 14:30",
  "participants": "John Doe",
  "sample": ["Hello", "How are you?", "Thanks!"],
  "conversationType": "individual",
  "createdAt": 1702642200000,
  "deviceId": "device_android_id",
  "appVersion": "1.0"
}
```

### For Group Conversations:

```json
{
  "timestamp": "15/12/2024 14:30",
  "group": "Family Group",
  "participants": "John, Jane, Bob",
  "sample": ["Hello everyone!", "How's it going?"],
  "conversationType": "group",
  "createdAt": 1702642200000,
  "deviceId": "device_android_id",
  "appVersion": "1.0"
}
```

## Features

### Automatic Storage
- Conversations are automatically stored in Firestore when detected
- Each conversation gets a unique document ID
- Metadata is automatically added (timestamp, device ID, app version)

### Error Handling
- Firebase operations run in background threads
- Errors are logged but don't crash the app
- Graceful fallback if Firebase is not available

### Batch Operations
- Support for storing multiple conversations at once
- Efficient batch writes to Firestore

## Troubleshooting

### Firebase Not Initializing
- Check that `google-services.json` is in the correct location
- Verify the package name matches your Firebase project
- Check internet connectivity

### Data Not Appearing in Firestore
- Check Firestore security rules
- Verify the collection name is "whatsapp"
- Check Android logs for Firebase errors

### Build Errors
- Clean and rebuild the project
- Check that all Firebase dependencies are properly added
- Verify Google Services plugin is applied

## Security Considerations

1. **Authentication**: Consider implementing user authentication for production
2. **Security Rules**: Implement proper Firestore security rules
3. **Data Privacy**: Ensure compliance with privacy regulations
4. **Network Security**: Use HTTPS for all Firebase communications

## Monitoring

You can monitor your Firestore usage in the Firebase Console:
- Go to "Firestore Database" > "Usage" tab
- Monitor read/write operations
- Set up billing alerts if needed

## Next Steps

1. Set up proper authentication
2. Implement security rules
3. Add data validation
4. Set up monitoring and alerts
5. Consider data retention policies

## Support

For issues with Firebase integration:
1. Check the Firebase documentation
2. Review Android logs for error messages
3. Verify your Firebase project configuration
4. Test with a simple Firestore write operation
