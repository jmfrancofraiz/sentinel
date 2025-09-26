package com.sentinel.service;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Firebase Firestore service for storing WhatsApp conversation data.
 * This service handles all Firestore operations including document creation,
 * batch writes, and error handling.
 */
public class FirebaseService {
    
    private static final String TAG = "FirebaseService";
    private static final String COLLECTION_NAME = "whatsapp";
    
    private FirebaseFirestore firestore;
    private FirebaseAuthService authService;
    private ExecutorService executorService;
    private Context context;
    private volatile boolean isInitialized = false;
    
    public FirebaseService(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.authService = new FirebaseAuthService(context);
        initializeFirestore();
    }
    
    /**
     * Initialize Firebase Firestore
     */
    private void initializeFirestore() {
        try {
            firestore = FirebaseFirestore.getInstance();
            isInitialized = true;
            Log.d(TAG, "Firebase Firestore initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Firestore", e);
            isInitialized = false;
        }
    }
    
    /**
     * Store a single conversation document in Firestore
     * @param conversationData JSON object containing conversation data
     */
    public void storeConversation(JSONObject conversationData) {
        if (!isInitialized || firestore == null) {
            Log.e(TAG, "Firebase not initialized, cannot store conversation");
            return;
        }
        
        // Check authentication
        if (!authService.isAuthenticated()) {
            Log.w(TAG, "User not authenticated, attempting to authenticate...");
            authService.signInWithEmailAndPassword();
            
            // If still not authenticated after attempt, skip storing
            if (!authService.isAuthenticated()) {
                Log.e(TAG, "Authentication failed, cannot store conversation");
                return;
            }
        }
        
        executorService.execute(() -> {
            try {
                // Convert JSON to Map for Firestore
                Map<String, Object> data = jsonToMap(conversationData);
                
                // Add metadata
                data.put("createdAt", System.currentTimeMillis());
                data.put("deviceId", getDeviceId());
                data.put("appVersion", getAppVersion());
                
                // Get collection name from config
                String collectionName = getCollectionName();
                
                // Store in Firestore
                firestore.collection(collectionName)
                    .add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "Conversation stored successfully with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error storing conversation", e);
                        }
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error processing conversation data", e);
            }
        });
    }
    
    /**
     * Store multiple conversations in a batch write
     * @param conversations Array of JSON objects containing conversation data
     */
    public void storeConversationsBatch(JSONObject[] conversations) {
        if (!isInitialized || firestore == null) {
            Log.e(TAG, "Firebase not initialized, cannot store conversations");
            return;
        }
        
        if (conversations == null || conversations.length == 0) {
            Log.w(TAG, "No conversations to store");
            return;
        }
        
        // Check authentication
        if (!authService.isAuthenticated()) {
            Log.w(TAG, "User not authenticated, attempting to authenticate...");
            authService.signInWithEmailAndPassword();
            
            // If still not authenticated after attempt, skip storing
            if (!authService.isAuthenticated()) {
                Log.e(TAG, "Authentication failed, cannot store conversations");
                return;
            }
        }
        
        executorService.execute(() -> {
            try {
                WriteBatch batch = firestore.batch();
                
                String collectionName = getCollectionName();
                
                for (JSONObject conversationData : conversations) {
                    Map<String, Object> data = jsonToMap(conversationData);
                    
                    // Add metadata
                    data.put("createdAt", System.currentTimeMillis());
                    data.put("deviceId", getDeviceId());
                    data.put("appVersion", getAppVersion());
                    data.put("userId", authService.getCurrentUserId());
                    data.put("userEmail", authService.getCurrentUser() != null ? 
                        authService.getCurrentUser().getEmail() : "unknown");
                    
                    // Add to batch
                    DocumentReference docRef = firestore.collection(collectionName).document();
                    batch.set(docRef, data);
                }
                
                // Commit batch
                batch.commit()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Batch write completed successfully for " + conversations.length + " conversations");
                            } else {
                                Log.e(TAG, "Batch write failed", task.getException());
                            }
                        }
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error processing batch conversations", e);
            }
        });
    }
    
    /**
     * Store conversation with custom document ID
     * @param documentId Custom document ID
     * @param conversationData JSON object containing conversation data
     */
    public void storeConversationWithId(String documentId, JSONObject conversationData) {
        if (!isInitialized || firestore == null) {
            Log.e(TAG, "Firebase not initialized, cannot store conversation");
            return;
        }
        
        if (documentId == null || documentId.trim().isEmpty()) {
            Log.e(TAG, "Document ID cannot be null or empty");
            return;
        }
        
        // Check authentication
        if (!authService.isAuthenticated()) {
            Log.w(TAG, "User not authenticated, attempting to authenticate...");
            authService.signInWithEmailAndPassword();
            
            // If still not authenticated after attempt, skip storing
            if (!authService.isAuthenticated()) {
                Log.e(TAG, "Authentication failed, cannot store conversation");
                return;
            }
        }
        
        executorService.execute(() -> {
            try {
                Map<String, Object> data = jsonToMap(conversationData);
                
                // Add metadata
                data.put("createdAt", System.currentTimeMillis());
                data.put("deviceId", getDeviceId());
                data.put("appVersion", getAppVersion());
                
                // Get collection name from config
                String collectionName = getCollectionName();
                
                firestore.collection(collectionName)
                    .document(documentId)
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Conversation stored successfully with custom ID: " + documentId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error storing conversation with custom ID: " + documentId, e);
                        }
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error processing conversation data with custom ID", e);
            }
        });
    }
    
    /**
     * Convert JSONObject to Map for Firestore
     */
    private Map<String, Object> jsonToMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        
        try {
            // Get all keys from JSON object
            java.util.Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                
                // Handle different data types
                if (value instanceof JSONObject) {
                    map.put(key, jsonToMap((JSONObject) value));
                } else if (value instanceof org.json.JSONArray) {
                    map.put(key, jsonArrayToList((org.json.JSONArray) value));
                } else if (value instanceof String[]) {
                    // Convert String array to List for Firestore compatibility
                    map.put(key, java.util.Arrays.asList((String[]) value));
                } else {
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting JSON to Map", e);
        }
        
        return map;
    }
    
    /**
     * Convert JSONArray to List for Firestore
     */
    private java.util.List<Object> jsonArrayToList(org.json.JSONArray jsonArray) {
        java.util.List<Object> list = new java.util.ArrayList<>();
        
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Object value = jsonArray.get(i);
                
                if (value instanceof JSONObject) {
                    list.add(jsonToMap((JSONObject) value));
                } else if (value instanceof org.json.JSONArray) {
                    list.add(jsonArrayToList((org.json.JSONArray) value));
                } else {
                    list.add(value);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting JSONArray to List", e);
        }
        
        return list;
    }
    
    /**
     * Get device ID for metadata
     */
    private String getDeviceId() {
        try {
            return android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
            );
        } catch (Exception e) {
            Log.e(TAG, "Error getting device ID", e);
            return "unknown";
        }
    }
    
    /**
     * Get app version for metadata
     */
    private String getAppVersion() {
        try {
            return context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0)
                .versionName;
        } catch (Exception e) {
            Log.e(TAG, "Error getting app version", e);
            return "unknown";
        }
    }
    
    /**
     * Check if Firebase is initialized and ready
     */
    public boolean isReady() {
        return isInitialized && firestore != null;
    }
    
    /**
     * Get collection name from configuration
     */
    private String getCollectionName() {
        try {
            return context.getString(com.sentinel.R.string.firebase_collection_name);
        } catch (Exception e) {
            Log.w(TAG, "Error getting collection name from config, using default", e);
            return COLLECTION_NAME;
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (authService != null) {
            authService.cleanup();
        }
        Log.d(TAG, "Firebase service cleaned up");
    }
}
