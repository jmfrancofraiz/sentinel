package com.sentinel.config;

import android.content.Context;
import android.content.res.Resources;

/**
 * Service configuration management.
 * Loads hardcoded configuration from Android resources.
 */
public class ServiceConfig {
    
    private static final String TAG = "ServiceConfig";
    
    private final Context context;
    private final Resources resources;
    
    // Configuration values
    private final String targetPackage;
    private final String logTag;
    
    public ServiceConfig(Context context) {
        this.context = context;
        this.resources = context.getResources();
        
        // Load configuration from resources
        this.targetPackage = loadStringConfig("target_package");
        this.logTag = loadStringConfig("log_tag");
    }
    
    // Getters for configuration values
    public String getTargetPackage() {
        return targetPackage;
    }
    
    public String getLogTag() {
        return logTag;
    }
    
    /**
     * Load string configuration from resources
     */
    private String loadStringConfig(String key) {
        try {
            int resourceId = resources.getIdentifier(key, "string", context.getPackageName());
            if (resourceId != 0) {
                return resources.getString(resourceId);
            }
        } catch (Exception e) {
            // Log error but don't crash
        }
        return getDefaultStringValue(key);
    }
    
    
    /**
     * Get default string values
     */
    private String getDefaultStringValue(String key) {
        switch (key) {
            case "target_package": return "com.whatsapp";
            case "log_tag": return "Sentinel";
            default: return "";
        }
    }
}
