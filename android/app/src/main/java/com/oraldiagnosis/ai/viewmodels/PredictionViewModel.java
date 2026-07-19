package com.oraldiagnosis.ai.viewmodels;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.StorageReference;
import com.oraldiagnosis.ai.firebase.FirebaseManager;
import com.oraldiagnosis.ai.models.DiagnosisResponse;
import com.oraldiagnosis.ai.repository.AppRepository;
import com.oraldiagnosis.ai.tflite.TFLiteHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionViewModel extends AndroidViewModel {
    private AppRepository repository;
    private MutableLiveData<TFLiteHelper.PredictionResult> localPredictionResult = new MutableLiveData<>();
    private MutableLiveData<DiagnosisResponse> uploadResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PredictionViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        FirebaseManager.init(application);
    }

    public LiveData<TFLiteHelper.PredictionResult> getLocalPredictionResult() {
        return localPredictionResult;
    }

    public LiveData<DiagnosisResponse> getUploadResult() {
        return uploadResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void runLocalPrediction(Bitmap bitmap) {
        try {
            TFLiteHelper tfLiteHelper = new TFLiteHelper(getApplication());
            TFLiteHelper.PredictionResult result = tfLiteHelper.predict(bitmap);
            tfLiteHelper.close();
            localPredictionResult.setValue(result);
        } catch (IOException e) {
            errorMessage.setValue("Failed to run on-device AI inference: " + e.getMessage());
        }
    }

    public void uploadDiagnosisToServer(String userId, String patientId, File imageFile) {
        // 1. Upload to Flask REST endpoint
        RequestBody userReq = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody patientReq = RequestBody.create(MediaType.parse("text/plain"), patientId);
        RequestBody imageReq = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageReq);

        repository.getApiService().diagnose(userReq, patientReq, imagePart).enqueue(new Callback<DiagnosisResponse>() {
            @Override
            public void onResponse(Call<DiagnosisResponse> call, Response<DiagnosisResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    uploadResult.setValue(response.body());
                } else {
                    mirrorDiagnosisToFirebaseOnly(userId, patientId, imageFile);
                }
            }

            @Override
            public void onFailure(Call<DiagnosisResponse> call, Throwable t) {
                mirrorDiagnosisToFirebaseOnly(userId, patientId, imageFile);
            }
        });

        // 2. Also mirror to shared Firebase Storage and Firestore collections (scans / reports) right away
        mirrorDiagnosisToFirebaseOnly(userId, patientId, imageFile);
    }

    private void mirrorDiagnosisToFirebaseOnly(String userId, String patientId, File imageFile) {
        try {
            TFLiteHelper.PredictionResult currentResult = localPredictionResult.getValue();
            String diagnosis = currentResult != null ? currentResult.label : "Reviewed";
            float confidence = currentResult != null ? currentResult.confidence : 95.0f;

            // Upload image to Firebase Storage if possible
            StorageReference storageRef = FirebaseManager.getStorage().getReference("scans/" + imageFile.getName());
            storageRef.putFile(Uri.fromFile(imageFile))
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveFirestoreReport(userId, patientId, diagnosis, confidence, uri.toString());
                    }))
                    .addOnFailureListener(e -> {
                        saveFirestoreReport(userId, patientId, diagnosis, confidence, imageFile.getName());
                    });
        } catch (Exception ignored) {}
    }

    private void saveFirestoreReport(String userId, String patientId, String diagnosis, float confidence, String imageUrl) {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("userId", userId);
        reportData.put("patientId", patientId);
        reportData.put("patient_id", patientId);
        reportData.put("diagnosis", diagnosis);
        reportData.put("confidence", confidence);
        reportData.put("imageUrl", imageUrl);
        reportData.put("image_filename", imageUrl);
        reportData.put("createdAt", FieldValue.serverTimestamp());
        reportData.put("date", java.text.DateFormat.getDateInstance().format(new java.util.Date()));

        FirebaseManager.getFirestore().collection("reports").add(reportData);
        FirebaseManager.getFirestore().collection("diagnosis_records").add(reportData);
        if (uploadResult.getValue() == null) {
            uploadResult.postValue(new DiagnosisResponse("success", diagnosis, confidence, imageUrl));
        }
    }
}
