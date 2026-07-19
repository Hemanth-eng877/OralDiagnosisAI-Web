package com.oraldiagnosis.ai.viewmodels;

import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oraldiagnosis.ai.firebase.FirebaseManager;
import com.oraldiagnosis.ai.models.AuthRequest;
import com.oraldiagnosis.ai.models.AuthResponse;
import com.oraldiagnosis.ai.repository.AppRepository;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {
    private AppRepository repository;
    private MutableLiveData<AuthResponse> loginResult = new MutableLiveData<>();
    private MutableLiveData<AuthResponse> signupResult = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        FirebaseManager.init(application);
    }

    public LiveData<AuthResponse> getLoginResult() {
        return loginResult;
    }

    public LiveData<AuthResponse> getSignupResult() {
        return signupResult;
    }

    public void login(String email, String password) {
        // Attempt Flask REST API first, then sync/verify with Firebase Auth and Firestore for shared web/android data
        repository.getApiService().login(new AuthRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().status)) {
                    logAuthEvent(FirebaseAnalytics.Event.LOGIN, email, true);
                    loginResult.setValue(response.body());
                } else {
                    // Fallback: Check direct Firebase Authentication (in case account was created on web directly via Firebase)
                    authenticateViaFirebase(email, password, false);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // If local Flask server is offline or unreachable, try direct Firebase Auth
                authenticateViaFirebase(email, password, false);
            }
        });
    }

    public void signup(String name, String email, String password) {
        repository.getApiService().signup(new AuthRequest(name, email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().status)) {
                    logAuthEvent(FirebaseAnalytics.Event.SIGN_UP, email, true);
                    // Also mirror in Firebase Auth and Firestore users collection
                    mirrorToFirebase(name, email, password, response.body().user_id);
                    signupResult.setValue(response.body());
                } else {
                    authenticateViaFirebase(email, password, true, name);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                authenticateViaFirebase(email, password, true, name);
            }
        });
    }

    private void authenticateViaFirebase(String email, String password, boolean isSignup, String... optionalName) {
        FirebaseAuth auth = FirebaseManager.getAuth();
        if (isSignup) {
            String name = optionalName.length > 0 ? optionalName[0] : "Doctor";
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser() != null ? authResult.getUser().getUid() : "u_" + System.currentTimeMillis();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("role", "Doctor");
                        userData.put("created_at", com.google.firebase.firestore.FieldValue.serverTimestamp());

                        FirebaseManager.getFirestore().collection("users").document(uid).set(userData);
                        logAuthEvent(FirebaseAnalytics.Event.SIGN_UP, email, true);
                        signupResult.setValue(new AuthResponse("success", "Account created in Firebase", uid, name));
                    })
                    .addOnFailureListener(e -> {
                        logAuthEvent(FirebaseAnalytics.Event.SIGN_UP, email, false);
                        signupResult.setValue(new AuthResponse("error", "Firebase Signup failed: " + e.getMessage(), null, null));
                    });
        } else {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser() != null ? authResult.getUser().getUid() : "u_" + System.currentTimeMillis();
                        FirebaseManager.getFirestore().collection("users").document(uid).get()
                                .addOnSuccessListener(doc -> {
                                    String name = doc.exists() && doc.getString("name") != null ? doc.getString("name") : "Doctor";
                                    logAuthEvent(FirebaseAnalytics.Event.LOGIN, email, true);
                                    loginResult.setValue(new AuthResponse("success", "Logged in via Firebase", uid, name));
                                })
                                .addOnFailureListener(e -> {
                                    logAuthEvent(FirebaseAnalytics.Event.LOGIN, email, true);
                                    loginResult.setValue(new AuthResponse("success", "Logged in via Firebase", uid, "Doctor"));
                                });
                    })
                    .addOnFailureListener(e -> {
                        logAuthEvent(FirebaseAnalytics.Event.LOGIN, email, false);
                        loginResult.setValue(new AuthResponse("error", "Authentication failed: Check email or password", null, null));
                    });
        }
    }

    private void mirrorToFirebase(String name, String email, String password, String userId) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("role", "Doctor");
            userData.put("created_at", com.google.firebase.firestore.FieldValue.serverTimestamp());
            if (userId != null) {
                FirebaseManager.getFirestore().collection("users").document(userId).set(userData);
            }
        } catch (Exception ignored) {}
    }

    private void logAuthEvent(String eventType, String email, boolean success) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.METHOD, "email_password");
            bundle.putString("email", email);
            bundle.putBoolean("success", success);
            FirebaseManager.getAnalytics(getApplication()).logEvent(eventType, bundle);
        } catch (Exception ignored) {}
    }
}
