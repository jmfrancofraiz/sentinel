package com.sentinel;

import android.app.Application;
import android.util.Log;

import com.sentinel.utils.LoggingManager;

/**
 * Main Application class for Sentinel
 * This is a UI-less app that only runs as an accessibility service
 */
public class SentinelApplication extends Application {
    
    private static final String TAG = "SentinelApplication";
    
    private LoggingManager loggingManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Sentinel application started");
        
        try {
            // Initialize core components
            initializeComponents();
            Log.d(TAG, "Application initialization completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error during application initialization", e);
        }
    }
    
    /**
     * Initialize core application components
     */
    private void initializeComponents() {
        Log.d(TAG, "Initializing application components...");
        
        // Initialize logging manager
        loggingManager = new LoggingManager(this);
        Log.d(TAG, "LoggingManager initialized");
        
        // Log application startup
        if (loggingManager != null) {
            loggingManager.logInfo("APPLICATION", "Sentinel application started successfully");
        }
        
        Log.d(TAG, "All application components initialized");
    }
    
    /**
     * Get the logging manager instance
     */
    public LoggingManager getLoggingManager() {
        return loggingManager;
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Sentinel application terminating");
        
        // Cleanup resources
        if (loggingManager != null) {
            loggingManager.cleanup();
        }
        
        Log.d(TAG, "Application cleanup completed");
    }
}
