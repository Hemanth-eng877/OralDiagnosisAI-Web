package com.oraldiagnosis.ai.api;

import android.content.Context;
import android.content.SharedPreferences;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:5000/";
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = DEFAULT_BASE_URL;

    public static synchronized Retrofit getClient() {
        return getClient(DEFAULT_BASE_URL);
    }

    public static synchronized Retrofit getClient(Context context) {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String savedUrl = prefs.getString("base_url", DEFAULT_BASE_URL);
            if (!savedUrl.endsWith("/")) {
                savedUrl += "/";
            }
            return getClient(savedUrl);
        }
        return getClient(DEFAULT_BASE_URL);
    }

    public static synchronized Retrofit getClient(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        if (retrofit == null || !currentBaseUrl.equals(baseUrl)) {
            currentBaseUrl = baseUrl;
            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static synchronized void resetClient() {
        retrofit = null;
    }
}
