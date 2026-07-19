package com.oraldiagnosis.ai.firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static boolean isInitialized = false;
    private static FirebaseAnalytics firebaseAnalytics;

    public static synchronized void init(Context context) {
        if (!isInitialized && context != null) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context.getApplicationContext());
                    Log.i(TAG, "Firebase initialized successfully with oraldiagnosisai project configuration.");
                }
                firebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
                isInitialized = true;
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            }
        }
    }

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }

    public static FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance();
    }

    public static FirebaseMessaging getMessaging() {
        return FirebaseMessaging.getInstance();
    }

    public static FirebaseAnalytics getAnalytics(Context context) {
        if (firebaseAnalytics == null && context != null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
        }
        return firebaseAnalytics;
    }
}
