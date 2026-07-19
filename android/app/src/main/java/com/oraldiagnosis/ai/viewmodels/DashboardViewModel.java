package com.oraldiagnosis.ai.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.oraldiagnosis.ai.models.DashboardResponse;
import com.oraldiagnosis.ai.repository.AppRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardViewModel extends AndroidViewModel {
    private AppRepository repository;
    private MutableLiveData<DashboardResponse> dashboardData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
    }

    public LiveData<DashboardResponse> getDashboardData() {
        return dashboardData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadDashboard(String userId) {
        repository.getApiService().getDashboard(userId).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dashboardData.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to load dashboard data.");
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }
}
