package com.oraldiagnosis.ai.ui.history;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.oraldiagnosis.ai.databinding.ActivityHistoryBinding;
import com.oraldiagnosis.ai.models.ReportItem;
import com.oraldiagnosis.ai.ui.dashboard.ReportAdapter;
import com.oraldiagnosis.ai.viewmodels.ReportViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private ActivityHistoryBinding binding;
    private ReportViewModel viewModel;
    private ReportAdapter adapter;
    private List<ReportItem> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        adapter = new ReportAdapter();
        binding.rvReports.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReports.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        viewModel.getReportsData().observe(this, response -> {
            if ("success".equals(response.status) && response.reports != null) {
                fullList = response.reports;
                adapter.setReports(fullList);
            }
        });

        viewModel.getErrorMessage().observe(this, err -> {
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (userId != null) {
            viewModel.loadReports(userId);
        }
    }

    private void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setReports(fullList);
            return;
        }
        String q = query.toLowerCase().trim();
        List<ReportItem> filtered = new ArrayList<>();
        for (ReportItem item : fullList) {
            String name = item.patient_name != null ? item.patient_name.toLowerCase() : "";
            if (name.contains(q) || (item.diagnosis != null && item.diagnosis.toLowerCase().contains(q))) {
                filtered.add(item);
            }
        }
        adapter.setReports(filtered);
    }
}
