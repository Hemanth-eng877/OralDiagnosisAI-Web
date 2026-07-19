package com.oraldiagnosis.ai.models;

public class DiagnosisResponse {
    public String status;
    public String message;
    public String diagnosis;
    public float confidence;
    public String image_filename;

    public DiagnosisResponse() {}

    public DiagnosisResponse(String status, String diagnosis, float confidence, String imageFilename) {
        this.status = status;
        this.diagnosis = diagnosis;
        this.confidence = confidence;
        this.image_filename = imageFilename;
    }
}
