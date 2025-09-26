package com.sentinel.service;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Firebase Authentication service for secure access to Firestore.
 * This service handles user authentication with email/password credentials
 * stored in the app configuration.
 */
public class FirebaseAuthService {
    
    private static final String TAG = "FirebaseAuthService";
    
    private FirebaseAuth firebaseAuth;
    private Context context;
    private ExecutorService executorService;
    private AtomicBoolean isAuthenticated = new AtomicBoolean(false);
    private AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    // Configuration values
    private String authEmail;
    private String authPassword;
    private boolean authEnabled;
    private boolean autoRetryAuth;
    
    public FirebaseAuthService(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        loadConfiguration();
        initializeAuth();
    }
    
    /**
     * Load authentication configuration from app resources
     */
    private void loadConfiguration() {
        try {
            authEmail = context.getString(com.sentinel.R.string.firebase_auth_email);
            authPassword = context.getString(com.sentinel.R.string.firebase_auth_password);
            authEnabled = context.getResources().getBoolean(com.sentinel.R.bool.firebase_auth_enabled);
            autoRetryAuth = context.getResources().getBoolean(com.sentinel.R.bool.firebase_auto_retry_auth);
            
            Log.d(TAG, "Authentication configuration loaded - Enabled: " + authEnabled);
        } catch (Exception e) {
            Log.e(TAG, "Error loading authentication configuration", e);
            authEnabled = false;
        }
    }
    
    /**
     * Initialize Firebase Authentication
     */
    private void initializeAuth() {
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            isInitialized.set(true);
            Log.d(TAG, "Firebase Authentication initialized");
            
            // Check if user is already signed in
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                isAuthenticated.set(true);
                Log.d(TAG, "User already authenticated: " + currentUser.getEmail());
            } else if (authEnabled) {
                // Attempt to sign in with configured credentials
                signInWithEmailAndPassword();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Authentication", e);
            isInitialized.set(false);
        }
    }
    
    /**
     * Sign in with email and password from configuration
     */
    public void signInWithEmailAndPassword() {
        if (!isInitialized.get() || !authEnabled) {
            Log.w(TAG, "Authentication not initialized or disabled");
            return;
        }
        
        if (authEmail == null || authPassword == null || 
            authEmail.trim().isEmpty() || authPassword.trim().isEmpty()) {
            Log.e(TAG, "Authentication credentials not configured properly");
            return;
        }
        
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Attempting to sign in with configured credentials...");
                
                firebaseAuth.signInWithEmailAndPassword(authEmail, authPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    isAuthenticated.set(true);
                                    Log.d(TAG, "Authentication successful for user: " + user.getEmail());
                                }
                            } else {
                                Log.e(TAG, "Authentication failed", task.getException());
                                isAuthenticated.set(false);
                                
                                // Retry authentication if enabled
                                if (autoRetryAuth) {
                                    Log.d(TAG, "Retrying authentication in 5 seconds...");
                                    executorService.execute(() -> {
                                        try {
                                            Thread.sleep(5000);
                                            signInWithEmailAndPassword();
                                        } catch (InterruptedException e) {
                                            Log.e(TAG, "Authentication retry interrupted", e);
                                        }
                                    });
                                }
                            }
                        }
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error during authentication", e);
                isAuthenticated.set(false);
            }
        });
    }
    
    /**
     * Sign out the current user
     */
    public void signOut() {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
            isAuthenticated.set(false);
            Log.d(TAG, "User signed out");
        }
    }
    
    /**
     * Create a new user account (for initial setup)
     */
    public void createUserAccount() {
        if (!isInitialized.get() || !authEnabled) {
            Log.w(TAG, "Authentication not initialized or disabled");
            return;
        }
        
        if (authEmail == null || authPassword == null || 
            authEmail.trim().isEmpty() || authPassword.trim().isEmpty()) {
            Log.e(TAG, "Authentication credentials not configured properly");
            return;
        }
        
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Creating new user account...");
                
                firebaseAuth.createUserWithEmailAndPassword(authEmail, authPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    isAuthenticated.set(true);
                                    Log.d(TAG, "User account created successfully: " + user.getEmail());
                                    
                                    // Update user profile
                                    updateUserProfile(user);
                                }
                            } else {
                                Log.e(TAG, "User account creation failed", task.getException());
                                isAuthenticated.set(false);
                            }
                        }
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error creating user account", e);
                isAuthenticated.set(false);
            }
        });
    }
    
    /**
     * Update user profile with display name
     */
    private void updateUserProfile(FirebaseUser user) {
        try {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName("WhatsApp Sentinel")
                .build();
                
            user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated successfully");
                        } else {
                            Log.w(TAG, "Failed to update user profile", task.getException());
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error updating user profile", e);
        }
    }
    
    /**
     * Check if user is currently authenticated
     */
    public boolean isAuthenticated() {
        return isAuthenticated.get() && firebaseAuth != null && firebaseAuth.getCurrentUser() != null;
    }
    
    /**
     * Get the current authenticated user
     */
    public FirebaseUser getCurrentUser() {
        if (firebaseAuth != null) {
            return firebaseAuth.getCurrentUser();
        }
        return null;
    }
    
    /**
     * Get the current user's UID
     */
    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }
    
    /**
     * Check if authentication is enabled in configuration
     */
    public boolean isAuthEnabled() {
        return authEnabled;
    }
    
    /**
     * Force re-authentication
     */
    public void reauthenticate() {
        if (authEnabled) {
            signOut();
            signInWithEmailAndPassword();
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        Log.d(TAG, "Firebase Authentication service cleaned up");
    }
}
