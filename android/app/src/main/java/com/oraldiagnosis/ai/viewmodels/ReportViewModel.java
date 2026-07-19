package com.oraldiagnosis.ai.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.oraldiagnosis.ai.firebase.FirebaseManager;
import com.oraldiagnosis.ai.models.ReportItem;
import com.oraldiagnosis.ai.models.ReportResponse;
import com.oraldiagnosis.ai.repository.AppRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportViewModel extends AndroidViewModel {
    private AppRepository repository;
    private MutableLiveData<ReportResponse> reportsData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ReportViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        FirebaseManager.init(application);
    }

    public LiveData<ReportResponse> getReportsData() {
        return reportsData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadReports(String userId) {
        // Fetch from shared Firestore 'reports' collection AND Flask REST API
        FirebaseManager.getFirestore().collection("reports")
                .get()
                .addOnSuccessListener(querySnap -> {
                    List<ReportItem> firestoreReports = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnap.getDocuments()) {
                        String patientId = doc.getString("patientId") != null ? doc.getString("patientId") : doc.getString("patient_id");
                        String patientName = doc.getString("patientName") != null ? doc.getString("patientName") : doc.getString("patient_name");
                        String diagnosis = doc.getString("diagnosis");
                        Double confDouble = doc.getDouble("confidence");
                        float confidence = confDouble != null ? confDouble.floatValue() : 0.0f;
                        String date = doc.getString("date") != null ? doc.getString("date") : (doc.get("createdAt") != null ? doc.get("createdAt").toString() : "");

                        firestoreReports.add(new ReportItem(doc.getId(), patientId, patientName, null, "", doc.getString("image_filename"), diagnosis, confidence, date));
                    }

                    // Merge with REST API results
                    repository.getApiService().getReports(userId).enqueue(new Callback<ReportResponse>() {
                        @Override
                        public void onResponse(Call<ReportResponse> call, Response<ReportResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().reports != null) {
                                for (ReportItem restR : response.body().reports) {
                                    boolean exists = false;
                                    for (ReportItem fr : firestoreReports) {
                                        if (fr.id != null && fr.id.equals(restR.id)) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) firestoreReports.add(restR);
                                }
                            }
                            reportsData.setValue(new ReportResponse("success", firestoreReports));
                        }

                        @Override
                        public void onFailure(Call<ReportResponse> call, Throwable t) {
                            reportsData.setValue(new ReportResponse("success", firestoreReports));
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    repository.getApiService().getReports(userId).enqueue(new Callback<ReportResponse>() {
                        @Override
                        public void onResponse(Call<ReportResponse> call, Response<ReportResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                reportsData.setValue(response.body());
                            } else {
                                errorMessage.setValue("Failed to load diagnosis reports.");
                            }
                        }

                        @Override
                        public void onFailure(Call<ReportResponse> call, Throwable t) {
                            errorMessage.setValue("Network error: " + t.getMessage());
                        }
                    });
                });
    }
}
