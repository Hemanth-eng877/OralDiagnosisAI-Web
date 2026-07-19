package com.oraldiagnosis.ai.api;

import com.oraldiagnosis.ai.models.AuthRequest;
import com.oraldiagnosis.ai.models.AuthResponse;
import com.oraldiagnosis.ai.models.DashboardResponse;
import com.oraldiagnosis.ai.models.DiagnosisResponse;
import com.oraldiagnosis.ai.models.GenericResponse;
import com.oraldiagnosis.ai.models.PatientItem;
import com.oraldiagnosis.ai.models.PatientResponse;
import com.oraldiagnosis.ai.models.ReportResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/signup")
    Call<AuthResponse> signup(@Body AuthRequest request);

    @GET("api/dashboard")
    Call<DashboardResponse> getDashboard(@Query("user_id") String userId);

    @GET("api/reports")
    Call<ReportResponse> getReports(@Query("user_id") String userId);

    @GET("api/patients")
    Call<PatientResponse> getPatients(@Query("user_id") String userId);

    @POST("api/patients/add")
    Call<GenericResponse> addPatient(@Body PatientItem patient);

    @PUT("api/patients/{id}/edit")
    Call<GenericResponse> editPatient(@Path("id") String id, @Body PatientItem patient);

    @DELETE("api/patients/{id}/delete")
    Call<GenericResponse> deletePatient(@Path("id") String id, @Query("user_id") String userId);

    @Multipart
    @POST("api/diagnose")
    Call<DiagnosisResponse> diagnose(
            @Part("user_id") RequestBody userId,
            @Part("patient_id") RequestBody patientId,
            @Part MultipartBody.Part image
    );
}
