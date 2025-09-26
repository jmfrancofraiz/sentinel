# Sentinel Safety Monitor

A Java-based Android application that monitors WhatsApp activity to track contact interactions using accessibility services and local LLM processing.

## Features

- **Privacy-First**: All processing happens locally on the device
- **No UI**: Headless service with hardcoded configuration
- **Intelligent Detection**: LLM-based contact name extraction
- **Battery Efficient**: Optimized for minimal resource usage
- **Reliable**: Accessibility service-based monitoring

## Requirements

- Android 5.0 (API level 21) or higher
- 2GB+ RAM recommended
- Accessibility permissions required

## Installation

1. Clone the repository
2. Open in Android Studio
3. Build and install the APK
4. Grant accessibility permissions in Android settings
5. The service will start automatically

## Configuration

All configuration is hardcoded in `app/src/main/res/values/config.xml`:

- Target package: `com.whatsapp`
- LLM confidence threshold: `0.85`
- Max elements per scan: `100`
- Scan interval: `500ms`

## Architecture

### Core Components

- **WhatsAppMonitoringService**: Main accessibility service
- **ContactDetector**: LLM-based contact name extraction
- **ModelManager**: TensorFlow Lite model management
- **ValidationEngine**: Contact name validation
- **LoggingManager**: Android logcat logging

### Data Flow

```
Accessibility Event → WhatsApp Detection → Element Traversal → 
Context Gathering → LLM Inference → Validation → Logcat Logging
```

## Usage

The service runs automatically once installed and permissions are granted. Contact names are logged to Android logcat with the tag "Sentinel".

### Logcat Output

```
I/Sentinel: Sentinel monitoring service started
I/Sentinel: Contact detected: John Doe
I/Sentinel: Contact detected: Jane Smith
```

## Development

### Project Structure

```
app/
├── src/main/
│   ├── java/com/sentinel/
│   │   ├── service/          # Accessibility service
│   │   ├── intelligence/     # LLM integration
│   │   ├── utils/           # Utility classes
│   │   └── config/          # Configuration
│   ├── res/                 # Android resources
│   └── assets/models/       # LLM model files
```

### Building

```bash
./gradlew assembleDebug
```

### Testing

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Privacy

- No data is transmitted to external servers
- All processing occurs locally on the device
- Contact names are only logged to system logcat
- No persistent storage of personal data

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## Support

For issues and questions, please create an issue in the repository.
