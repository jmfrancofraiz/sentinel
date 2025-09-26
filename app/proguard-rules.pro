# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep all application classes
-keep class com.whatsappsentinel.** { *; }

# Keep accessibility service classes
-keep class com.whatsappsentinel.service.** { *; }

# Keep intelligence classes
-keep class com.whatsappsentinel.intelligence.** { *; }

# Keep utility classes
-keep class com.whatsappsentinel.utils.** { *; }

# Keep configuration classes
-keep class com.whatsappsentinel.config.** { *; }

# TensorFlow Lite rules
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }
-keep class org.tensorflow.lite.metadata.** { *; }

# Keep TensorFlow Lite native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep TensorFlow Lite model files
-keep class **.R
-keep class **.R$*
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Keep Timber logging
-keep class timber.log.** { *; }

# Keep AndroidX classes
-keep class androidx.** { *; }

# Keep accessibility service
-keep class * extends android.accessibilityservice.AccessibilityService {
    public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent);
    public void onInterrupt();
    public void onServiceConnected();
}

# Keep model assets
-keep class com.whatsappsentinel.R$raw { *; }
-keep class com.whatsappsentinel.R$string { *; }

# Keep reflection-based classes
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove Timber logging in release builds
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
