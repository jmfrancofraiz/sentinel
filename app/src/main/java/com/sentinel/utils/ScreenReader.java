package com.sentinel.utils;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Utility class for reading full screen content and converting it to JSON.
 * Extracts all accessible elements from the current screen.
 */
public class ScreenReader {
    
    private static final String TAG = "ScreenReader";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    
    /**
     * Format current timestamp as dd/MM/yyyy hh:mm
     */
    public static String getFormattedTimestamp() {
        return DATE_FORMAT.format(new Date());
    }
    
    /**
     * Helper class for counting elements
     */
    private static class ElementCounts {
        int textElements = 0;
        int clickableElements = 0;
        int editTextElements = 0;
        int buttonElements = 0;
        int imageElements = 0;
        int maxDepth = 0;
    }
    
    /**
     * Read the full screen content and convert to JSON
     */
    public static JSONObject readFullScreen(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return createErrorJson("Root node is null");
        }
        
        try {
            JSONObject screenData = new JSONObject();
            screenData.put("timestamp", getFormattedTimestamp());
            screenData.put("packageName", rootNode.getPackageName() != null ? rootNode.getPackageName().toString() : "unknown");
            screenData.put("className", rootNode.getClassName() != null ? rootNode.getClassName().toString() : "unknown");
            
            // Extract all UI elements
            JSONArray elements = extractAllElements(rootNode);
            screenData.put("elements", elements);
            screenData.put("totalElements", elements.length());
            
            // Extract screen metadata
            JSONObject metadata = extractScreenMetadata(rootNode);
            screenData.put("metadata", metadata);
            
            // Extract clean TextView texts
            JSONArray cleanTexts = extractCleanTextViewTexts(rootNode);
            screenData.put("cleanTexts", cleanTexts);
            
            // Extract contact/group name
            String contactOrGroup = findFirstTextViewText(rootNode);
            screenData.put("contactOrGroup", contactOrGroup != null ? contactOrGroup : "");
            
            return screenData;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating screen JSON", e);
            return createErrorJson("JSON creation failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract all accessible elements from the screen
     */
    private static JSONArray extractAllElements(AccessibilityNodeInfo rootNode) throws JSONException {
        JSONArray elements = new JSONArray();
        
        // Traverse the entire node tree
        traverseNode(rootNode, elements, 0);
        
        return elements;
    }
    
    /**
     * Recursively traverse nodes and extract element data
     */
    private static void traverseNode(AccessibilityNodeInfo node, JSONArray elements, int depth) throws JSONException {
        if (node == null) {
            return;
        }
        
        try {
            JSONObject element = new JSONObject();
            
            // Basic element information
            element.put("depth", depth);
            element.put("className", node.getClassName() != null ? node.getClassName().toString() : "");
            element.put("text", node.getText() != null ? node.getText().toString() : "");
            element.put("contentDescription", node.getContentDescription() != null ? node.getContentDescription().toString() : "");
            element.put("hint", node.getHintText() != null ? node.getHintText().toString() : "");
            
            // Action information
            int actions = node.getActions();
            if (actions != 0) {
                element.put("actions", actions);
            }
            
            // State information
            element.put("clickable", node.isClickable());
            element.put("focusable", node.isFocusable());
            element.put("selected", node.isSelected());
            element.put("checked", node.isChecked());
            element.put("enabled", node.isEnabled());
            element.put("visible", node.isVisibleToUser());
            
            // Resource ID
            if (node.getViewIdResourceName() != null) {
                element.put("resourceId", node.getViewIdResourceName());
            }
            
            // Bounds
            android.graphics.Rect bounds = new android.graphics.Rect();
            node.getBoundsInScreen(bounds);
            JSONObject boundsObj = new JSONObject();
            boundsObj.put("left", bounds.left);
            boundsObj.put("top", bounds.top);
            boundsObj.put("right", bounds.right);
            boundsObj.put("bottom", bounds.bottom);
            boundsObj.put("width", bounds.width());
            boundsObj.put("height", bounds.height());
            element.put("bounds", boundsObj);
            
            // Add element to array
            elements.put(element);
            
            // Process children
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    traverseNode(child, elements, depth + 1);
                }
            }
            
        } finally {
            // Note: We don't recycle the node here as it might be used by the caller
        }
    }
    
    /**
     * Extract screen metadata
     */
    private static JSONObject extractScreenMetadata(AccessibilityNodeInfo rootNode) throws JSONException {
        JSONObject metadata = new JSONObject();
        
        // Count different types of elements
        ElementCounts counts = countElements(rootNode);
        metadata.put("textElements", counts.textElements);
        metadata.put("clickableElements", counts.clickableElements);
        metadata.put("editTextElements", counts.editTextElements);
        metadata.put("buttonElements", counts.buttonElements);
        metadata.put("imageElements", counts.imageElements);
        metadata.put("totalDepth", counts.maxDepth);
        
        // Extract all visible text
        List<String> visibleTexts = extractVisibleTexts(rootNode);
        JSONArray textsArray = new JSONArray();
        for (String text : visibleTexts) {
            if (text != null && !text.trim().isEmpty()) {
                textsArray.put(text.trim());
            }
        }
        metadata.put("visibleTexts", textsArray);
        
        return metadata;
    }
    
    /**
     * Count different types of elements
     */
    private static ElementCounts countElements(AccessibilityNodeInfo node) {
        ElementCounts counts = new ElementCounts();
        countElementsRecursive(node, counts, 0);
        return counts;
    }
    
    /**
     * Recursively count elements
     */
    private static void countElementsRecursive(AccessibilityNodeInfo node, ElementCounts counts, int depth) {
        if (node == null) {
            return;
        }
        
        counts.maxDepth = Math.max(counts.maxDepth, depth);
        
        // Count by type
        if (node.getText() != null && !node.getText().toString().trim().isEmpty()) {
            counts.textElements++;
        }
        
        if (node.isClickable()) {
            counts.clickableElements++;
        }
        
        CharSequence className = node.getClassName();
        if (className != null) {
            String classNameStr = className.toString();
            if (classNameStr.contains("EditText")) {
                counts.editTextElements++;
            } else if (classNameStr.contains("Button")) {
                counts.buttonElements++;
            } else if (classNameStr.contains("Image")) {
                counts.imageElements++;
            }
        }
        
        // Process children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                countElementsRecursive(child, counts, depth + 1);
            }
        }
    }
    
    /**
     * Extract all visible text from the screen
     */
    private static List<String> extractVisibleTexts(AccessibilityNodeInfo node) {
        List<String> texts = new ArrayList<>();
        extractVisibleTextsRecursive(node, texts);
        return texts;
    }
    
    /**
     * Recursively extract visible texts
     */
    private static void extractVisibleTextsRecursive(AccessibilityNodeInfo node, List<String> texts) {
        if (node == null) {
            return;
        }
        
        // Add text content
        if (node.getText() != null && !node.getText().toString().trim().isEmpty()) {
            texts.add(node.getText().toString());
        }
        
        // Add content description
        if (node.getContentDescription() != null && !node.getContentDescription().toString().trim().isEmpty()) {
            texts.add(node.getContentDescription().toString());
        }
        
        // Add hint text
        if (node.getHintText() != null && !node.getHintText().toString().trim().isEmpty()) {
            texts.add(node.getHintText().toString());
        }
        
        // Process children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                extractVisibleTextsRecursive(child, texts);
            }
        }
    }
    
    /**
     * Extract clean TextView texts, filtering out UI elements
     */
    private static JSONArray extractCleanTextViewTexts(AccessibilityNodeInfo rootNode) throws JSONException {
        JSONArray cleanTexts = new JSONArray();
        
        // UI elements to filter out
        Set<String> filterOut = new HashSet<>(Arrays.asList(
            "Atrás", "Videollamada", "Llamada", "Más opciones", 
            "Emojis, GIF y stickers", "Adjuntar", "Cámara", "Mensaje",
            "Botón de mensajes de voz", "Leído", "Entregado", "Enviado"
        ));
        
        // Get all TextView elements
        List<TextViewData> textViews = collectTextViewElements(rootNode);
        
        for (TextViewData textView : textViews) {
            String text = textView.text;
            
            if (text != null && !text.trim().isEmpty()) {
                String trimmed = text.trim();
                
                // Skip filtered elements
                if (!filterOut.contains(trimmed)) {
                    cleanTexts.put(trimmed);
                }
            }
        }
        
        Log.d(TAG, "Extracted " + cleanTexts.length() + " clean TextView texts");
        return cleanTexts;
    }
    
    /**
     * Helper class to store TextView data with position information
     */
    private static class TextViewData {
        String text;
        int position;
        
        TextViewData(String text, int position) {
            this.text = text;
            this.position = position;
        }
    }
    
    /**
     * Collect all TextView elements from the screen in document order
     */
    private static List<TextViewData> collectTextViewElements(AccessibilityNodeInfo rootNode) {
        List<TextViewData> textViews = new ArrayList<>();
        collectTextViewElementsRecursive(rootNode, textViews);
        return textViews;
    }
    
    /**
     * Recursively collect TextView elements in document order
     */
    private static void collectTextViewElementsRecursive(AccessibilityNodeInfo node, List<TextViewData> textViews) {
        if (node == null) {
            return;
        }
        
        // Check if this is a TextView
        CharSequence className = node.getClassName();
        if (className != null && className.toString().equals("android.widget.TextView")) {
            // Get text from multiple sources
            String text = null;
            
            // Primary text content
            CharSequence nodeText = node.getText();
            if (nodeText != null && !nodeText.toString().trim().isEmpty()) {
                text = nodeText.toString();
            }
            
            // If no text, try content description
            if (text == null || text.trim().isEmpty()) {
                CharSequence contentDesc = node.getContentDescription();
                if (contentDesc != null && !contentDesc.toString().trim().isEmpty()) {
                    text = contentDesc.toString();
                }
            }
            
            // Add to list if we have text
            if (text != null && !text.trim().isEmpty()) {
                textViews.add(new TextViewData(text.trim(), textViews.size()));
                Log.v(TAG, "Added TextView: '" + text.trim() + "'");
            }
        }
        
        // Process children in order
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectTextViewElementsRecursive(child, textViews);
            }
        }
    }

    /**
     * Extract contact or group name from the first android.widget.TextView element
     * This is specifically designed for WhatsApp Conversation activity
     */
    public static JSONObject extractContactOrGroupName(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return createErrorJson("Root node is null");
        }
        
        try {
            JSONObject contactData = new JSONObject();
            contactData.put("timestamp", getFormattedTimestamp());
            
            // Find the first TextView element
            String contactOrGroup = findFirstTextViewText(rootNode);
            contactData.put("contactOrGroup", contactOrGroup != null ? contactOrGroup : "");
            
            return contactData;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating contact JSON", e);
            return createErrorJson("Contact extraction failed: " + e.getMessage());
        }
    }
    
    /**
     * Recursively find the first TextView element and extract its text
     */
    private static String findFirstTextViewText(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }
        
        // Check if this is a TextView
        CharSequence className = node.getClassName();
        if (className != null && className.toString().equals("android.widget.TextView")) {
            // Get the text content
            CharSequence text = node.getText();
            if (text != null && !text.toString().trim().isEmpty()) {
                return text.toString().trim();
            }
        }
        
        // Search in children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                String result = findFirstTextViewText(child);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }

    /**
     * Create error JSON
     */
    private static JSONObject createErrorJson(String errorMessage) {
        JSONObject errorJson = new JSONObject();
        try {
            errorJson.put("timestamp", getFormattedTimestamp());
            errorJson.put("error", errorMessage);
            errorJson.put("elements", new JSONArray());
            errorJson.put("totalElements", 0);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating error JSON", e);
        }
        return errorJson;
    }
}
