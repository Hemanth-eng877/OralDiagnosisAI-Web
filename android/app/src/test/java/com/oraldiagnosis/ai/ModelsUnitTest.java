package com.oraldiagnosis.ai;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.oraldiagnosis.ai.models.AuthRequest;
import com.oraldiagnosis.ai.models.AuthResponse;
import com.oraldiagnosis.ai.models.DashboardResponse;
import com.oraldiagnosis.ai.models.PatientItem;
import com.oraldiagnosis.ai.models.ReportItem;

import java.util.ArrayList;

public class ModelsUnitTest {
    private final Gson gson = new Gson();

    @Test
    public void authRequest_Serialization() {
        AuthRequest request = new AuthRequest("Dr. Smith", "smith@gmail.com", "secret123");
        String json = gson.toJson(request);
        assertTrue(json.contains("smith@gmail.com"));
        assertTrue(json.contains("Dr. Smith"));
    }

    @Test
    public void authResponse_Deserialization() {
        String json = "{\"status\":\"success\",\"message\":\"Logged in\",\"user_id\":\"u101\",\"user_name\":\"Dr. Smith\"}";
        AuthResponse resp = gson.fromJson(json, AuthResponse.class);
        assertEquals("success", resp.status);
        assertEquals("u101", resp.user_id);
        assertEquals("Dr. Smith", resp.user_name);
    }

    @Test
    public void patientItem_ModelConsistency() {
        PatientItem patient = new PatientItem("p1", "u101", "John Doe", 45, "Male", "555-0199", "john@example.com", "Smoker");
        assertEquals("John Doe", patient.toString());
        assertEquals(Integer.valueOf(45), patient.age);
    }

    @Test
    public void reportItem_ModelConsistency() {
        ReportItem report = new ReportItem("r1", "p1", "John Doe", 45, "Male", "img1.jpg", "Normal", 98.5f, "2026-07-19");
        assertEquals("Normal", report.diagnosis);
        assertEquals(98.5f, report.confidence, 0.01f);
    }

    @Test
    public void dashboardResponse_Deserialization() {
        String json = "{\"status\":\"success\",\"patient_count\":10,\"report_count\":5,\"recent_reports\":[]}";
        DashboardResponse resp = gson.fromJson(json, DashboardResponse.class);
        assertEquals(10, resp.patient_count);
        assertEquals(5, resp.report_count);
        assertNotNull(resp.recent_reports);
    }
}
