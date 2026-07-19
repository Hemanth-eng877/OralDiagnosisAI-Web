package com.oraldiagnosis.ai.models;

public class AuthResponse {
    public String status;
    public String message;
    public String user_id;
    public String user_name;

    public AuthResponse() {}

    public AuthResponse(String status, String message, String userId, String userName) {
        this.status = status;
        this.message = message;
        this.user_id = userId;
        this.user_name = userName;
    }
}
