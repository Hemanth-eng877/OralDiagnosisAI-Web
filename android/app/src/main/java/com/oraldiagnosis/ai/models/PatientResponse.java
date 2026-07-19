package com.oraldiagnosis.ai.models;

import java.util.List;

public class PatientResponse {
    public String status;
    public String message;
    public List<PatientItem> patients;

    public PatientResponse() {}

    public PatientResponse(String status, List<PatientItem> patients) {
        this.status = status;
        this.patients = patients;
    }
}
