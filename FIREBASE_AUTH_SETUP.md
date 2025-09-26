# Firebase Authentication Setup for WhatsApp Sentinel

This guide will help you set up secure Firebase Firestore integration with authentication for the WhatsApp Sentinel project.

## 🔐 Security Features

- **User Authentication**: Email/password authentication required
- **Data Isolation**: Users can only access their own data
- **Secure Rules**: Firestore security rules prevent unauthorized access
- **Configurable Credentials**: Authentication credentials stored in app configuration

## Prerequisites

1. A Google account
2. Android Studio with Firebase support
3. The WhatsApp Sentinel project already set up

## Step 1: Create Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" or "Add project"
3. Enter project name (e.g., "whatsapp-sentinel-secure")
4. Choose whether to enable Google Analytics (optional)
5. Click "Create project"

## Step 2: Enable Authentication

1. In your Firebase project, go to "Authentication"
2. Click "Get started"
3. Go to "Sign-in method" tab
4. Enable "Email/Password" authentication
5. Click "Save"

## Step 3: Add Android App to Firebase

1. In your Firebase project, click "Add app" and select Android
2. Enter your package name: `com.sentinel`
3. Enter app nickname: "WhatsApp Sentinel"
4. Enter SHA-1 certificate fingerprint (optional for debug builds)
5. Click "Register app"

## Step 4: Download Configuration File

1. Download the `google-services.json` file
2. Place it at `app/google-services.json` (replace the template)

## Step 5: Configure Authentication Credentials

1. Open `app/src/main/res/values/config.xml`
2. Update the authentication credentials:

```xml
<!-- Firebase Authentication Configuration -->
<string name="firebase_auth_email">your-email@example.com</string>
<string name="firebase_auth_password">your-secure-password</string>
```

3. Update other configuration as needed:

```xml
<!-- Firebase Collection Configuration -->
<string name="firebase_collection_name">whatsapp</string>

<!-- Security Configuration -->
<bool name="firebase_auth_enabled">true</bool>
<bool name="firebase_auto_retry_auth">true</bool>
```

## Step 6: Create User Account

### Option A: Create Account via Firebase Console
1. Go to Authentication > Users tab
2. Click "Add user"
3. Enter email and password
4. Click "Add user"

### Option B: Create Account via App (First Run)
The app will automatically attempt to create a user account on first run if the credentials don't exist.

## Step 7: Configure Firestore Security Rules

1. Go to "Firestore Database" in your Firebase project
2. Click "Rules" tab
3. Replace the default rules with the secure rules from `firestore_security_rules.txt`:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /whatsapp/{document} {
      allow read, write: if request.auth != null 
        && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null 
        && request.auth.uid == request.resource.data.userId;
    }
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

4. Click "Publish"

## Step 8: Build and Test

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

5. Check the logs to see authentication and Firebase integration:
   ```bash
   adb logcat | grep -E "(FirebaseAuthService|FirebaseService|WhatsAppSentinel)"
   ```

## Data Structure

The conversation data will be stored in Firestore with the following secure structure:

### Collection: `whatsapp` (configurable)

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

## Security Features

### Authentication
- ✅ Email/password authentication required
- ✅ Automatic re-authentication on failure
- ✅ User account creation on first run
- ✅ Credentials stored in app configuration

### Data Security
- ✅ Only authenticated users can access data
- ✅ Firestore security rules enforce authentication
- ✅ No anonymous access allowed
- ✅ Immutable data (no updates/deletes)

### Privacy
- ✅ No user identification stored in documents
- ✅ Device identification for tracking
- ✅ Timestamp tracking for data lifecycle
- ✅ Configurable collection names

## Configuration Options

### Authentication Settings
- `firebase_auth_enabled`: Enable/disable authentication (default: true)
- `firebase_auto_retry_auth`: Auto-retry authentication on failure (default: true)
- `firebase_auth_email`: Email for authentication
- `firebase_auth_password`: Password for authentication

### Collection Settings
- `firebase_collection_name`: Firestore collection name (default: "whatsapp")

## Troubleshooting

### Authentication Issues
- **"Authentication failed"**: Check email/password in config.xml
- **"User not authenticated"**: Verify Firebase project has Authentication enabled
- **"Invalid credentials"**: Ensure user account exists in Firebase Console

### Data Access Issues
- **"Permission denied"**: Check Firestore security rules
- **"No data appearing"**: Verify user is authenticated and rules allow access
- **"Collection not found"**: Check collection name in configuration

### Build Issues
- **"Google services not found"**: Ensure google-services.json is in correct location
- **"Authentication not initialized"**: Check Firebase dependencies in build.gradle
- **"Configuration error"**: Verify all required strings in config.xml

## Production Considerations

### Security
1. **Strong Passwords**: Use strong, unique passwords for authentication
2. **Regular Updates**: Keep Firebase SDK and app updated
3. **Monitor Access**: Use Firebase Console to monitor data access
4. **Backup Rules**: Keep security rules backed up and versioned

### Performance
1. **Batch Operations**: Use batch writes for multiple conversations
2. **Offline Support**: Consider implementing offline data caching
3. **Data Retention**: Implement data retention policies
4. **Monitoring**: Set up Firebase monitoring and alerts

### Compliance
1. **Data Privacy**: Ensure compliance with GDPR, CCPA, etc.
2. **Data Encryption**: Firebase encrypts data in transit and at rest
3. **Access Logs**: Monitor who accesses what data when
4. **Data Deletion**: Implement user data deletion capabilities

## Testing

Use the provided test script to verify authentication:

```bash
./test_firebase_integration.sh
```

Look for these success messages:
- ✅ "Firebase Authentication initialized"
- ✅ "Authentication successful for user: your-email@example.com"
- ✅ "Conversation stored successfully with ID: ..."

## Support

For issues with Firebase authentication:
1. Check Firebase Console for authentication errors
2. Review Android logs for detailed error messages
3. Verify Firestore security rules are properly configured
4. Test authentication with Firebase Console directly
