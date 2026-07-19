package com.oraldiagnosis.ai.models;

public class GenericResponse {
    public String status;
    public String message;
    public String id;

    public GenericResponse() {}

    public GenericResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
