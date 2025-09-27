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
 * Firebase Firestore service for storing WhatsApp interaction data.
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
     * Store a single interaction document in Firestore
     * @param interactionData JSON object containing interaction data
     */
    public void storeInteraction(JSONObject interactionData) {
        if (!isInitialized || firestore == null) {
            Log.e(TAG, "Firebase not initialized, cannot store interaction");
            return;
        }
        
        // Check authentication
        if (!authService.isAuthenticated()) {
            Log.w(TAG, "User not authenticated, attempting to authenticate...");
            authService.signInWithEmailAndPassword();
            
            // If still not authenticated after attempt, skip storing
            if (!authService.isAuthenticated()) {
                Log.e(TAG, "Authentication failed, cannot store interaction");
                return;
            }
        }
        
        executorService.execute(() -> {
            try {
                // Convert JSON to Map for Firestore
                Map<String, Object> data = jsonToMap(interactionData);
                
                // Add metadata
                data.put("createdAt", System.currentTimeMillis());
                data.put("deviceId", getDeviceId());
                data.put("appVersion", getAppVersion());
                data.put("userId", authService.getCurrentUserId());
                data.put("userEmail", authService.getCurrentUser() != null ? 
                    authService.getCurrentUser().getEmail() : "unknown");
                
                // Get the appropriate document reference based on interaction type
                DocumentReference docRef = getInteractionDocumentReference(data, null);
                
                // Store in Firestore: whatsapp/{userId}/conversations/{participant|group}/interactions/{conversationId}
                docRef.set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Interaction stored successfully at: " + docRef.getPath());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error storing interaction", e);
                        }
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error processing interaction data", e);
            }
        });
    }
    
    /**
     * Store multiple interactions in a batch write
     * @param interactions Array of JSON objects containing interaction data
     */
    public void storeInteractionsBatch(JSONObject[] interactions) {
        if (!isInitialized || firestore == null) {
            Log.e(TAG, "Firebase not initialized, cannot store interactions");
            return;
        }
        
        if (interactions == null || interactions.length == 0) {
            Log.w(TAG, "No interactions to store");
            return;
        }
        
        // Check authentication
        if (!authService.isAuthenticated()) {
            Log.w(TAG, "User not authenticated, attempting to authenticate...");
            authService.signInWithEmailAndPassword();
            
            // If still not authenticated after attempt, skip storing
            if (!authService.isAuthenticated()) {
                Log.e(TAG, "Authentication failed, cannot store interactions");
                return;
            }
        }
        
        executorService.execute(() -> {
            try {
                WriteBatch batch = firestore.batch();
                
                for (JSONObject interactionData : interactions) {
                    Map<String, Object> data = jsonToMap(interactionData);
                    
                    // Add metadata
                    data.put("createdAt", System.currentTimeMillis());
                    data.put("deviceId", getDeviceId());
                    data.put("appVersion", getAppVersion());
                    data.put("userId", authService.getCurrentUserId());
                    data.put("userEmail", authService.getCurrentUser() != null ? 
                        authService.getCurrentUser().getEmail() : "unknown");
                    
                    // Get the appropriate document reference based on interaction type
                    DocumentReference docRef = getInteractionDocumentReference(data, null);
                    batch.set(docRef, data);
                }
                
                // Commit batch
                batch.commit()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Batch write completed successfully for " + interactions.length + " interactions");
                            } else {
                                Log.e(TAG, "Batch write failed", task.getException());
                            }
                        }
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error processing batch interactions", e);
            }
        });
    }
    
    /**
     * Store interaction with custom document ID
     * @param documentId Custom document ID
     * @param interactionData JSON object containing interaction data
     */
    public void storeInteractionWithId(String documentId, JSONObject interactionData) {
        if (!isInitialized || firestore == null) {
            Log.e(TAG, "Firebase not initialized, cannot store interaction");
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
                Log.e(TAG, "Authentication failed, cannot store interaction");
                return;
            }
        }
        
        executorService.execute(() -> {
            try {
                Map<String, Object> data = jsonToMap(interactionData);
                
                // Add metadata
                data.put("createdAt", System.currentTimeMillis());
                data.put("deviceId", getDeviceId());
                data.put("appVersion", getAppVersion());
                data.put("userId", authService.getCurrentUserId());
                data.put("userEmail", authService.getCurrentUser() != null ? 
                    authService.getCurrentUser().getEmail() : "unknown");
                
                // Get the appropriate document reference based on interaction type with custom ID
                DocumentReference docRef = getInteractionDocumentReference(data, documentId);
                
                // Store in Firestore: whatsapp/{userId}/conversations/{participant|group}/interactions/{conversationId}
                docRef.set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Interaction stored successfully with custom ID at: " + docRef.getPath());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error storing interaction with custom ID: " + documentId, e);
                        }
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error processing interaction data with custom ID", e);
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
     * Get the appropriate Firestore path based on conversation type
     * @param interactionData The interaction data containing type and participant/group info
     * @param customInteractionId Custom interaction ID to use (if null, generates timestamp-based ID)
     * @return The appropriate DocumentReference for the interaction
     */
    private DocumentReference getInteractionDocumentReference(Map<String, Object> interactionData, String customInteractionId) {
        String collectionName = getCollectionName();
        String userId = authService.getCurrentUserId();
        
        // Get conversation type
        String conversationType = (String) interactionData.get("conversationType");
        if (conversationType == null) {
            conversationType = "individual"; // Default to individual if not specified
        }
        
        // Use custom ID or generate timestamp-based ID
        String interactionId = (customInteractionId != null && !customInteractionId.trim().isEmpty()) 
            ? customInteractionId 
            : String.valueOf(System.currentTimeMillis());
        
        if ("individual".equals(conversationType)) {
            // Individual conversation: whatsapp/{userId}/conversations/{participant}/interactions/{interactionId}
            String participant = (String) interactionData.get("participants");
            if (participant == null || participant.trim().isEmpty()) {
                participant = "unknown_participant";
            }
            // Sanitize participant name for Firestore document ID
            participant = sanitizeDocumentId(participant);
            
            return firestore.collection(collectionName)
                .document(userId)
                .collection("conversations")
                .document(participant)
                .collection("interactions")
                .document(interactionId);
        } else {
            // Group conversation: whatsapp/{userId}/conversations/{group}/interactions/{interactionId}
            String group = (String) interactionData.get("group");
            if (group == null || group.trim().isEmpty()) {
                group = "unknown_group";
            }
            // Sanitize group name for Firestore document ID
            group = sanitizeDocumentId(group);
            
            return firestore.collection(collectionName)
                .document(userId)
                .collection("conversations")
                .document(group)
                .collection("interactions")
                .document(interactionId);
        }
    }
    
    /**
     * Sanitize string for use as Firestore document ID
     * @param input The input string to sanitize
     * @return Sanitized string safe for use as document ID
     */
    private String sanitizeDocumentId(String input) {
        if (input == null) {
            return "unknown";
        }
        
        // Remove emojis and other Unicode symbols
        String sanitized = input.replaceAll("[\\p{So}\\p{Cn}\\p{Co}\\p{Cs}]", "")
                               // Replace invalid characters with underscores
                               // Firestore document IDs cannot contain: / \ ? # [ ] and cannot start with __
                               .replaceAll("[/\\\\?#\\[\\]]", "_")
                               .replaceAll("\\s+", "_")
                               .trim();
        
        // Ensure it doesn't start with __
        if (sanitized.startsWith("__")) {
            sanitized = "conversation_" + sanitized;
        }
        
        // Ensure it's not empty
        if (sanitized.isEmpty()) {
            sanitized = "unknown";
        }
        
        // Limit length to avoid issues
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }
        
        return sanitized;
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
