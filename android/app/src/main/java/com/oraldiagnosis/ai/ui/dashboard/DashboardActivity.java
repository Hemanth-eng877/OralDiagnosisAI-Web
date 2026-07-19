package com.oraldiagnosis.ai.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.oraldiagnosis.ai.databinding.ActivityDashboardBinding;
import com.oraldiagnosis.ai.models.ReportItem;
import com.oraldiagnosis.ai.ui.chatbot.ChatbotActivity;
import com.oraldiagnosis.ai.ui.diagnose.PredictionActivity;
import com.oraldiagnosis.ai.ui.history.HistoryActivity;
import com.oraldiagnosis.ai.ui.login.LoginActivity;
import com.oraldiagnosis.ai.ui.patients.PatientsActivity;
import com.oraldiagnosis.ai.ui.profile.ProfileActivity;
import com.oraldiagnosis.ai.ui.settings.SettingsActivity;
import com.oraldiagnosis.ai.viewmodels.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;
    private DashboardViewModel viewModel;
    private ReportAdapter adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        String userName = prefs.getString("user_name", "");
        if (userId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding.tvWelcome.setText(userName.isEmpty() ? "Welcome Doctor" : "Welcome, Dr. " + userName);

        adapter = new ReportAdapter();
        binding.rvRecentReports.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentReports.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        viewModel.getDashboardData().observe(this, response -> {
            if ("success".equals(response.status)) {
                binding.tvPatientCount.setText(String.valueOf(response.patient_count));
                binding.tvReportCount.setText(String.valueOf(response.report_count));
                
                List<ReportItem> items = new ArrayList<>();
                if (response.recent_reports != null) {
                    for (com.oraldiagnosis.ai.models.DashboardResponse.ReportItem r : response.recent_reports) {
                        items.add(new ReportItem(r.id, r.patient_id, "Patient #" + r.patient_id, null, "", "", r.diagnosis, r.confidence, ""));
                    }
                }
                adapter.setReports(items);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        binding.btnDiagnose.setOnClickListener(v -> startActivity(new Intent(this, PredictionActivity.class)));
        binding.btnPatients.setOnClickListener(v -> startActivity(new Intent(this, PatientsActivity.class)));
        binding.btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        binding.btnChatbot.setOnClickListener(v -> startActivity(new Intent(this, ChatbotActivity.class)));
        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null && viewModel != null) {
            viewModel.loadDashboard(userId);
        }
    }
}
