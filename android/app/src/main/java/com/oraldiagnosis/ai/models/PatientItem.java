package com.oraldiagnosis.ai.models;

import java.io.Serializable;

public class PatientItem implements Serializable {
    public String id;
    public String user_id;
    public String full_name;
    public Integer age;
    public String gender;
    public String phone;
    public String email;
    public String notes;
    public String created_at;

    public PatientItem() {}

    public PatientItem(String id, String userId, String fullName, Integer age, String gender, String phone, String email, String notes) {
        this.id = id;
        this.user_id = userId;
        this.full_name = fullName;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
    }

    @Override
    public String toString() {
        return full_name != null ? full_name : "Unknown Patient";
    }
}
