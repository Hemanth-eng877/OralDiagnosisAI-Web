package com.oraldiagnosis.ai.models;

import java.util.List;

public class ReportResponse {
    public String status;
    public String message;
    public List<ReportItem> reports;

    public ReportResponse() {}

    public ReportResponse(String status, List<ReportItem> reports) {
        this.status = status;
        this.reports = reports;
    }
}
