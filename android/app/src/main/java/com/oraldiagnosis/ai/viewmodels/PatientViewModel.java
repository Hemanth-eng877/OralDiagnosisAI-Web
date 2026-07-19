package com.oraldiagnosis.ai.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.oraldiagnosis.ai.firebase.FirebaseManager;
import com.oraldiagnosis.ai.models.GenericResponse;
import com.oraldiagnosis.ai.models.PatientItem;
import com.oraldiagnosis.ai.models.PatientResponse;
import com.oraldiagnosis.ai.repository.AppRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientViewModel extends AndroidViewModel {
    private AppRepository repository;
    private MutableLiveData<PatientResponse> patientsData = new MutableLiveData<>();
    private MutableLiveData<GenericResponse> actionResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PatientViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        FirebaseManager.init(application);
    }

    public LiveData<PatientResponse> getPatientsData() {
        return patientsData;
    }

    public LiveData<GenericResponse> getActionResult() {
        return actionResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadPatients(String userId) {
        // Query both Firestore directly (where web users store patients) and Flask REST API to ensure 100% shared data
        FirebaseManager.getFirestore().collection("patients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PatientItem> firestorePatients = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString("name") != null ? doc.getString("name") : doc.getString("full_name");
                        if (name == null) name = "Patient " + doc.getId();
                        Integer age = doc.getLong("age") != null ? doc.getLong("age").intValue() : null;
                        String gender = doc.getString("gender");
                        String phone = doc.getString("phone");
                        String email = doc.getString("email");
                        String notes = doc.getString("notes");

                        firestorePatients.add(new PatientItem(doc.getId(), userId, name, age, gender, phone, email, notes));
                    }

                    // Also fetch REST API patients and combine
                    repository.getApiService().getPatients(userId).enqueue(new Callback<PatientResponse>() {
                        @Override
                        public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().patients != null) {
                                for (PatientItem restP : response.body().patients) {
                                    boolean exists = false;
                                    for (PatientItem fp : firestorePatients) {
                                        if (fp.id != null && fp.id.equals(restP.id)) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) firestorePatients.add(restP);
                                }
                            }
                            patientsData.setValue(new PatientResponse("success", firestorePatients));
                        }

                        @Override
                        public void onFailure(Call<PatientResponse> call, Throwable t) {
                            // Even if Flask server is unreachable, we successfully return Firestore patients!
                            patientsData.setValue(new PatientResponse("success", firestorePatients));
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Fallback to purely Flask REST API if direct Firestore fetch fails
                    repository.getApiService().getPatients(userId).enqueue(new Callback<PatientResponse>() {
                        @Override
                        public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                patientsData.setValue(response.body());
                            } else {
                                errorMessage.setValue("Failed to fetch patients.");
                            }
                        }

                        @Override
                        public void onFailure(Call<PatientResponse> call, Throwable t) {
                            errorMessage.setValue("Network error: " + t.getMessage());
                        }
                    });
                });
    }

    public void addPatient(PatientItem patient) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", patient.full_name);
        data.put("full_name", patient.full_name);
        if (patient.age != null) data.put("age", patient.age);
        if (patient.gender != null) data.put("gender", patient.gender);
        if (patient.phone != null) data.put("phone", patient.phone);
        if (patient.email != null) data.put("email", patient.email);
        if (patient.notes != null) data.put("notes", patient.notes);
        if (patient.user_id != null) data.put("userId", patient.user_id);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        // Write directly to Firestore patients collection shared with Web app
        FirebaseManager.getFirestore().collection("patients")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    patient.id = documentReference.getId();
                    // Also invoke Flask REST endpoint
                    repository.getApiService().addPatient(patient).enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {}
                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {}
                    });
                    actionResult.setValue(new GenericResponse("success", "Patient added successfully to shared Firestore database."));
                })
                .addOnFailureListener(e -> {
                    // Fallback to REST API
                    repository.getApiService().addPatient(patient).enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                actionResult.setValue(response.body());
                            } else {
                                actionResult.setValue(new GenericResponse("error", "Failed to add patient."));
                            }
                        }
                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {
                            actionResult.setValue(new GenericResponse("error", "Network error: " + t.getMessage()));
                        }
                    });
                });
    }

    public void editPatient(String patientId, PatientItem patient) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", patient.full_name);
        data.put("full_name", patient.full_name);
        if (patient.age != null) data.put("age", patient.age);
        if (patient.gender != null) data.put("gender", patient.gender);
        if (patient.phone != null) data.put("phone", patient.phone);
        if (patient.email != null) data.put("email", patient.email);
        if (patient.notes != null) data.put("notes", patient.notes);
        data.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseManager.getFirestore().collection("patients").document(patientId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    repository.getApiService().editPatient(patientId, patient).enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {}
                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {}
                    });
                    actionResult.setValue(new GenericResponse("success", "Patient updated in shared Firestore database."));
                })
                .addOnFailureListener(e -> {
                    repository.getApiService().editPatient(patientId, patient).enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                actionResult.setValue(response.body());
                            } else {
                                actionResult.setValue(new GenericResponse("error", "Failed to edit patient."));
                            }
                        }
                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {
                            actionResult.setValue(new GenericResponse("error", "Network error: " + t.getMessage()));
                        }
                    });
                });
    }

    public void deletePatient(String patientId, String userId) {
        FirebaseManager.getFirestore().collection("patients").document(patientId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    repository.getApiService().deletePatient(patientId, userId).enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {}
                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {}
                    });
                    actionResult.setValue(new GenericResponse("success", "Patient deleted from shared Firestore database."));
                })
                .addOnFailureListener(e -> {
                    repository.getApiService().deletePatient(patientId, userId).enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                actionResult.setValue(response.body());
                            } else {
                                actionResult.setValue(new GenericResponse("error", "Failed to delete patient."));
                            }
                        }
                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {
                            actionResult.setValue(new GenericResponse("error", "Network error: " + t.getMessage()));
                        }
                    });
                });
    }
}
