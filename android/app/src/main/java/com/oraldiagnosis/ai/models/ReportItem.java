package com.oraldiagnosis.ai.models;

public class ReportItem {
    public String id;
    public String patient_id;
    public String patient_name;
    public Integer age;
    public String gender;
    public String image_filename;
    public String diagnosis;
    public float confidence;
    public String created_at;

    public ReportItem() {}

    public ReportItem(String id, String patientId, String patientName, Integer age, String gender, String imageFilename, String diagnosis, float confidence, String createdAt) {
        this.id = id;
        this.patient_id = patientId;
        this.patient_name = patientName;
        this.age = age;
        this.gender = gender;
        this.image_filename = imageFilename;
        this.diagnosis = diagnosis;
        this.confidence = confidence;
        this.created_at = createdAt;
    }
}
