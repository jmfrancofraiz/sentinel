# Sentinel - Model Testing Guide

## 🚀 Quick Start

### Option 1: Automated Testing
```bash
./install_and_test.sh
```

### Option 2: Manual Testing

1. **Install the APK:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Test the Model:**
   ```bash
   adb shell am start -n com.sentinel.debug/.ModelTestActivity
   ```

3. **View Logs:**
   ```bash
   adb logcat | grep Sentinel
   ```

## 📱 What to Expect

### Model Test Activity
When you launch the Model Test Activity, you should see:

**✅ SUCCESS:**
```
Model test completed!

Input: John Doe
Output: [extracted contact name]
Model Info: Input: [1, 200], Output: [1, 50]
```

**❌ FAILURE:**
```
Model test failed: [error message]
```

### Common Error Messages

1. **"Model file not found"** - The TensorFlow Lite model file is missing
2. **"Invalid input shape"** - Model input/output dimensions don't match
3. **"Model initialization failed"** - General model loading error
4. **"Inference engine not initialized"** - Model not properly loaded

## 🔧 Troubleshooting

### If Model Test Fails:

1. **Check Model File:**
   ```bash
   adb shell ls -la /data/app/com.whatsappsentinel.debug*/base.apk
   ```

2. **Check Logs for Details:**
   ```bash
   adb logcat | grep -E "(ModelManager|InferenceEngine|TensorFlow)"
   ```

3. **Verify Model File in APK:**
   ```bash
   unzip -l app/build/outputs/apk/debug/app-debug.apk | grep tflite
   ```

### If Accessibility Service Doesn't Work:

1. **Enable Service:**
   - Go to Settings > Accessibility > Downloaded apps
   - Find "WhatsApp Sentinel" and enable it

2. **Grant Permissions:**
   - Allow all requested permissions
   - Enable "Display over other apps" if prompted

3. **Test with WhatsApp:**
   - Open WhatsApp
   - Navigate to any chat
   - Check logs for contact detection

## 📊 Expected Log Output

### Successful Model Initialization:
```
D/ModelManager: Initializing LLM model...
D/InferenceEngine: Initializing TensorFlow Lite inference engine...
D/InferenceEngine: Loading model: models/contact_detection_model.tflite
D/InferenceEngine: Model file loaded, size: [size] bytes
D/InferenceEngine: TensorFlow Lite model loaded successfully
D/ModelManager: LLM model initialized successfully
```

### Successful Contact Extraction:
```
D/ModelManager: Extracting contact name from text: [text]
D/ModelManager: Contact name extracted: [name] (confidence: [score])
```

## 🧪 Testing Scenarios

### 1. Model Initialization Test
- Launch Model Test Activity
- Verify model loads without errors
- Check model info output

### 2. Contact Extraction Test
- Enable accessibility service
- Open WhatsApp
- Navigate to different chats
- Verify contact names are detected in logs

### 3. Error Handling Test
- Test with invalid inputs
- Verify proper error messages
- Check that fallbacks are not used

## 📝 Notes

- The model now works **without fallbacks** - it either works properly or throws clear errors
- All TensorFlow Lite dependencies are properly configured
- GPU acceleration is enabled when available
- The model file is included in the APK assets

## 🆘 If You Need Help

1. **Check the logs first** - they contain detailed error information
2. **Verify your Android version** - requires API 21+ (Android 5.0+)
3. **Check device compatibility** - TensorFlow Lite requires ARM or x86 architecture
4. **Ensure sufficient storage** - model requires ~200MB

## 🔄 Reinstalling

To reinstall after changes:
```bash
adb uninstall com.whatsappsentinel.debug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
