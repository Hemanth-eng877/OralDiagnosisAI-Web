package com.oraldiagnosis.ai.repository;

import android.content.Context;

import com.oraldiagnosis.ai.api.ApiClient;
import com.oraldiagnosis.ai.api.ApiService;

public class AppRepository {
    private ApiService apiService;
    private Context context;

    public AppRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public AppRepository(Context context) {
        this.context = context.getApplicationContext();
        apiService = ApiClient.getClient(this.context).create(ApiService.class);
    }

    public ApiService getApiService() {
        if (context != null) {
            apiService = ApiClient.getClient(context).create(ApiService.class);
        } else {
            apiService = ApiClient.getClient().create(ApiService.class);
        }
        return apiService;
    }
}
