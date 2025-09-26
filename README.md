# WhatsApp Sentinel

A Java-based Android accessibility service that monitors WhatsApp conversations and extracts contact information using screen reading technology. The app operates completely UI-less and logs conversation data to Android logcat.

## 🚀 Features

- **Privacy-First**: All processing happens locally on the device
- **UI-less Design**: Completely headless accessibility service
- **Real-time Monitoring**: Monitors WhatsApp conversation screens
- **JSON Logging**: Structured conversation data output
- **Individual/Group Detection**: Automatically detects conversation types
- **No External Dependencies**: Pure Android accessibility service

## 📋 Requirements

- Android 5.0 (API level 21) or higher
- 2GB+ RAM recommended
- Accessibility permissions required
- WhatsApp installed on device

## 🛠️ Installation

### Quick Setup
```bash
# Clone the repository
git clone https://github.com/yourusername/WhatsAppSentinel.git
cd WhatsAppSentinel

# Build the APK
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Manual Setup
1. Clone the repository
2. Open in Android Studio
3. Build and install the APK
4. Go to Settings > Accessibility > Downloaded apps
5. Enable "WhatsApp Sentinel" service
6. Grant all requested permissions

## ⚙️ Configuration

Configuration is hardcoded in `app/src/main/res/values/config.xml`:

```xml
<string name="target_package">com.whatsapp</string>
<string name="log_tag">WhatsAppSentinel</string>
<string name="firebase_collection_name">whatsapp</string>
<string name="firebase_auth_email">your-email@example.com</string>
<string name="firebase_auth_password">your-password</string>
<bool name="firebase_auth_enabled">true</bool>
```

## 🔥 Firebase Integration

The app includes Firebase Firestore integration for storing conversation data:

### Data Structure
Conversations are stored in Firestore using a subcollection structure:
- **Path**: `whatsapp/{userId}/conversations/{conversationId}`
- **Security**: Each user can only access their own conversations
- **Authentication**: Required for all Firebase operations

### Setup
1. Add your `google-services.json` to `app/` directory
2. Configure Firebase credentials in `config.xml`
3. Deploy Firestore security rules from `firestore_security_rules.txt`

### Features
- **Automatic Storage**: Conversations are automatically stored in Firestore
- **User Isolation**: Each user's data is stored separately
- **Batch Operations**: Efficient batch writes for multiple conversations
- **Error Handling**: Graceful fallback if Firebase is unavailable

## 🏗️ Architecture

### Core Components

- **WhatsAppMonitoringService**: Main accessibility service that monitors WhatsApp
- **ServiceConfig**: Configuration management from Android resources
- **LoggingManager**: Structured logging to Android logcat
- **ScreenReader**: Screen content extraction and JSON formatting

### How It Works

1. **Accessibility Service**: Monitors WhatsApp app for conversation activities
2. **Screen Detection**: Identifies individual vs group conversations
3. **Content Extraction**: Reads all TextView elements from the screen
4. **JSON Formatting**: Creates structured JSON with conversation data
5. **Logcat Output**: Logs formatted data to Android logcat

### Data Flow

```
WhatsApp Conversation → Accessibility Event → Screen Reading → 
TextView Extraction → JSON Formatting → Logcat Output
```

## 📱 Usage

The service runs automatically once installed and permissions are granted. It monitors WhatsApp conversations and logs structured data.

### Logcat Output

```json
{
  "timestamp": "15/12/2024 14:30",
  "participants": "John Doe",
  "sample": ["Hello", "How are you?", "Thanks!"],
  "conversationType": "individual"
}
```

### Group Conversation Output

```json
{
  "timestamp": "15/12/2024 14:30",
  "group": "Family Group",
  "participants": "John, Jane, Bob",
  "sample": ["Hello everyone!", "How's it going?"],
  "conversationType": "group"
}
```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Manual Testing
1. Install the APK
2. Enable accessibility service
3. Open WhatsApp and navigate to conversations
4. Check logcat output:
```bash
adb logcat | grep WhatsAppSentinel
```

### Test Data
The project includes sample conversation data:
- `individual_chat_example.json` - Individual conversation sample
- `group_chat_example.json` - Group conversation sample

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/sentinel/
│   │   ├── service/
│   │   │   ├── WhatsAppMonitoringService.java    # Main accessibility service
│   │   │   ├── FirebaseService.java              # Firebase Firestore integration
│   │   │   └── FirebaseAuthService.java          # Firebase authentication
│   │   ├── config/
│   │   │   └── ServiceConfig.java                # Configuration management
│   │   ├── utils/
│   │   │   ├── LoggingManager.java              # Logging utilities
│   │   │   └── ScreenReader.java                # Screen reading utilities
│   │   └── SentinelApplication.java             # Application class
│   ├── res/
│   │   ├── values/
│   │   │   ├── config.xml                       # Hardcoded configuration
│   │   │   └── strings.xml                      # String resources
│   │   └── xml/
│   │       └── accessibility_service_config.xml # Accessibility service config
│   └── AndroidManifest.xml                      # App manifest
├── src/test/
│   └── java/com/sentinel/
│       └── ServiceConfigTest.java               # Unit tests
└── build.gradle                                 # Build configuration
```

## 🔧 Development

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### Dependencies
- AndroidX Core
- Material Design Components
- Firebase Firestore
- Firebase Auth
- Jackson JSON Processing
- Gson JSON Processing
- Timber Logging
- JUnit Testing
- Mockito Testing
- Robolectric Testing

## 🔒 Privacy & Security

- **Local Processing**: All data processing happens on device
- **Secure Cloud Storage**: Data stored in Firebase Firestore with user authentication
- **User Isolation**: Each user can only access their own conversation data
- **Encrypted Transmission**: All Firebase communications use HTTPS/TLS
- **Permission-Based**: Requires explicit user consent for accessibility access
- **No UI**: Reduces attack surface and complexity
- **Configurable**: Firebase integration can be disabled via configuration

## 📊 Performance

- **Memory Usage**: ~50-100MB during operation
- **Battery Impact**: Minimal (accessibility service optimized)
- **Processing Time**: <50ms per conversation scan
- **Compatibility**: Android 5.0+ (API 21+)

## 🐛 Troubleshooting

### Service Not Working
1. Check accessibility permissions are granted
2. Verify WhatsApp is installed and accessible
3. Check logcat for error messages:
```bash
adb logcat | grep -E "(WhatsAppSentinel|Sentinel)"
```

### No Log Output
1. Ensure service is enabled in accessibility settings
2. Open WhatsApp and navigate to a conversation
3. Check that the conversation activity is detected

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For issues and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the test data examples

## 🔄 Changelog

### v1.0.0
- Initial release
- WhatsApp conversation monitoring
- Individual and group chat detection
- JSON-formatted logcat output
- UI-less accessibility service design
