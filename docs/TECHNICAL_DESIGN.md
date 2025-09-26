# Sentinel Safety Monitor - Technical Design

## Overview

This document outlines the technical design for a Java-based Android application that monitors WhatsApp activity to track contact interactions using accessibility services and local LLM processing.

## Core Architecture

### Technology Stack
- **Language**: Java (Android native)
- **UI**: Completely UI-less (headless service)
- **Monitoring**: Android Accessibility Service
- **Intelligence**: Local LLM (TensorFlow Lite)
- **Logging**: Android Logcat system
- **Configuration**: Hardcoded in Android resources

## 1. Accessibility Service Foundation

### Service Design
The application uses a custom `AccessibilityService` as the core monitoring mechanism.

**Key Components:**
- Custom `AccessibilityService` class that monitors WhatsApp UI changes
- Event filtering focused on specific accessibility event types
- Package-based detection for WhatsApp-specific monitoring
- Chat screen identification through UI pattern recognition

**Event Types Monitored:**
```java
- TYPE_WINDOW_CONTENT_CHANGED
- TYPE_WINDOW_STATE_CHANGED  
- TYPE_VIEW_FOCUSED
- TYPE_VIEW_SELECTED
```

**Why This Approach:**
- **Reliability**: Accessibility Service provides stable access to UI elements
- **Completeness**: Can monitor all UI changes in real-time
- **Android Native**: Well-supported API with good documentation
- **Permission-Based**: User explicitly grants access, ensuring transparency

### WhatsApp Detection Mechanism

**Package-Based Detection:**
- Monitor for `com.whatsapp` package name in window changes
- Track activity transitions to identify chat screens vs. other screens
- Maintain state machine for WhatsApp navigation flow

**Chat Screen Identification:**
- Detect specific UI patterns that indicate chat screen (not chat list)
- Look for characteristic elements like message input field, contact header
- Use element hierarchy analysis to confirm chat context

## 2. LLM-Only Contact Detection

### Intelligence Layer
The system uses a local LLM for all contact name extraction, eliminating regex patterns entirely.

**Model Specifications:**
- **Base Model**: MobileBERT or DistilBERT with NER fine-tuning
- **Model Size**: 150-200MB (optimized for mobile)
- **Input**: UI element text + surrounding context (up to 200 characters)
- **Output**: Contact name with confidence score (>0.85 threshold)
- **Framework**: TensorFlow Lite with GPU acceleration support

**Why LLM-Only (No Regex):**
- **Robustness**: Handles UI changes, international characters, complex text structures
- **Accuracy**: Contextual understanding prevents false positives
- **Maintainability**: No pattern updates needed for WhatsApp changes
- **Future-Proof**: Adapts automatically to new UI patterns
- **Internationalization**: Native support for all character sets and name formats

### Context-Aware Processing Pipeline

**Multi-Element Context Gathering:**
```java
// Gather context from multiple UI elements
- Primary element text (potential contact name)
- Parent element text (for context)
- Sibling elements (for validation)
- UI element type and position information
```

**Enhanced Input Preprocessing:**
```java
// Contextual text preparation
- Combine multiple element texts with separators
- Add UI element metadata (type, position, hierarchy)
- Include timestamp and screen state information
- Normalize text while preserving context
```

**Intelligent Output Processing:**
```java
// Multi-stage validation
- Confidence scoring (>0.85 threshold)
- Context validation (is this actually a contact name?)
- Format validation (reasonable name characteristics)
- Duplicate detection across recent extractions
```

## 3. Processing Pipeline

### Real-Time Flow
```
Accessibility Event → WhatsApp Detection → Element Traversal → 
Context Gathering → LLM Inference → Validation → Logcat Logging
```

### Key Processing Steps

1. **Event Filtering**: Only process WhatsApp-related UI changes
2. **Element Traversal**: Recursively scan UI elements for potential contact names
3. **Context Gathering**: Collect surrounding elements for better understanding
4. **LLM Inference**: Process text with confidence scoring
5. **Validation**: Multi-stage validation (confidence, format, context)
6. **Logging**: Output to Android Logcat with timestamp and context

### Smart Processing Strategy

**Intelligent Element Filtering:**
```java
// Pre-filter elements before LLM processing
- Skip elements with very short text (<3 characters)
- Skip elements with only numbers or symbols
- Skip elements that are clearly not contact names (timestamps, buttons)
- Focus on action bar, toolbar, and header elements
```

**Batch Processing Optimization:**
```java
// Process multiple elements together
- Group related UI elements for context
- Batch inference for multiple candidates
- Cache results for similar text patterns
- Implement smart caching with TTL
```

## 4. Performance Optimization

### Battery Efficiency
- **Adaptive Processing**: High frequency during active chat, reduced during idle
- **Smart Filtering**: Skip obviously non-contact elements before LLM processing
- **Background Management**: Pause when WhatsApp not in foreground
- **Model Optimization**: INT8 quantization, GPU acceleration when available

### Memory Management
- **Lazy Loading**: Load LLM model only when needed
- **Memory Mapping**: Efficient model asset loading
- **Caching Strategy**: Smart caching of recent results and context
- **Cleanup**: Automatic resource cleanup under memory pressure

### CPU Usage Minimization
- **Efficient Algorithms**: Optimized text processing and context gathering
- **Background Threading**: Use background threads for heavy processing
- **Early Termination**: Stop processing when high-confidence result found
- **Resource Monitoring**: Track and limit CPU usage

## 5. Error Handling and Resilience

### Service Resilience
**Permission Management:**
- Detect accessibility permission revocation
- Implement graceful degradation
- Provide user feedback through system notifications

**Model Loading Failures:**
```java
// Graceful degradation strategies
- Retry model loading with exponential backoff
- Fallback to basic text extraction (log raw text)
- Notify user through system notification
- Implement model validation on startup
```

**Inference Failures:**
```java
// Handle inference errors gracefully
- Retry failed inferences with different parameters
- Log raw text when inference fails completely
- Implement circuit breaker pattern for repeated failures
- Fallback to simple text logging
```

### Quality Assurance

**Multi-Stage Validation Pipeline:**
```java
// Multi-factor confidence assessment
- Model confidence score (>0.85)
- Context consistency score
- Format validation score
- Temporal consistency score
- Combined confidence threshold (>0.8)
```

**False Positive Prevention:**
```java
// Prevent logging non-contact text
- Validate against known non-contact patterns
- Check for system messages and timestamps
- Verify against recent conversation context
- Implement blacklist for common false positives
```

## 6. Configuration and Resources

### Hardcoded Configuration
**Resource File Structure:**
```xml
<!-- res/values/config.xml -->
<resources>
    <string name="target_package">com.whatsapp</string>
    <string name="log_tag">WhatsAppSentinel</string>
    <integer name="max_elements_per_scan">100</integer>
    <integer name="scan_interval_ms">500</integer>
    <float name="llm_confidence_threshold">0.8</float>
</resources>
```

**Runtime Configuration Loading:**
- Load configuration once during service initialization
- Cache configuration values in memory
- No runtime configuration changes (as per requirements)

### Resource Management
- **Model Assets**: Store LLM model in `assets/` directory
- **String Resources**: Use Android string resources for all text content
- **Memory Management**: Efficient asset loading and cleanup
- **Storage**: 200MB for model assets

## 7. Key Technical Decisions

### Why Java (Not Kotlin)
- Explicit requirement from project specifications
- Better compatibility with existing Android accessibility APIs
- More straightforward integration with TensorFlow Lite

### Why No UI
- Security and privacy considerations
- Simpler deployment and maintenance
- Focus on core monitoring functionality
- Reduced attack surface

### Why Local LLM (Not Cloud)
- Privacy protection (no data transmission)
- Offline operation capability
- No network dependency
- Better battery efficiency (no network calls)

### Why Logcat Logging
- Standard Android logging mechanism
- Easy debugging and monitoring
- No persistent storage requirements
- System-level integration

## 8. Expected Performance Characteristics

### Resource Usage
- **Memory**: 200-300MB (including LLM model)
- **Battery**: <2% additional drain during active monitoring
- **CPU**: <5% additional usage during chat interactions
- **Storage**: 200MB for model assets

### Accuracy Targets
- **Contact Detection**: >95% accuracy for contact name extraction
- **False Positives**: <1% false positive rate
- **Processing Time**: <100ms per contact detection
- **Reliability**: 99%+ uptime during WhatsApp usage

## 9. Security and Privacy Considerations

### Data Handling
- **Local Processing**: All LLM processing happens locally
- **No Data Transmission**: No contact data leaves the device
- **Minimal Storage**: Only logs to system logcat
- **Permission-Based**: Requires explicit user consent

### Security Measures
- **Accessibility Permission**: Users must explicitly grant access
- **No UI**: Reduces attack surface
- **Hardcoded Config**: No external configuration files
- **Local Only**: No network communication

## 10. Compatibility and Maintenance

### Android Version Support
- **Minimum SDK**: API 21+ (Android 5.0) for accessibility features
- **Target SDK**: Latest Android version
- **Compatibility**: Tested across major Android versions

### WhatsApp Compatibility
- **Version Adaptation**: Designed to handle WhatsApp UI changes
- **Pattern Learning**: LLM adapts to new UI patterns
- **Fallback Strategies**: Graceful handling of UI changes

### Maintenance Strategy
- **Model Updates**: Periodic model retraining for accuracy
- **UI Adaptation**: Automatic adaptation to WhatsApp changes
- **Performance Monitoring**: Continuous optimization based on usage patterns

## Conclusion

This technical design provides a robust, privacy-focused, and efficient solution for monitoring WhatsApp contact interactions. The LLM-only approach ensures high accuracy and adaptability while maintaining minimal resource impact and strong privacy protection through local processing.
