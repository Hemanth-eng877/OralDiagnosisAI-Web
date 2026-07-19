package com.oraldiagnosis.ai.ui.patients;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.oraldiagnosis.ai.databinding.ActivityPatientsBinding;
import com.oraldiagnosis.ai.models.PatientItem;
import com.oraldiagnosis.ai.viewmodels.PatientViewModel;

public class PatientsActivity extends AppCompatActivity {
    private ActivityPatientsBinding binding;
    private PatientViewModel viewModel;
    private PatientAdapter adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);

        adapter = new PatientAdapter(new PatientAdapter.OnPatientActionListener() {
            @Override
            public void onEdit(PatientItem patient) {
                Intent intent = new Intent(PatientsActivity.this, PatientFormActivity.class);
                intent.putExtra("patient", patient);
                startActivity(intent);
            }

            @Override
            public void onDelete(PatientItem patient) {
                if (userId != null && patient.id != null) {
                    viewModel.deletePatient(patient.id, userId);
                }
            }
        });

        binding.rvPatients.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPatients.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        viewModel.getPatientsData().observe(this, response -> {
            if ("success".equals(response.status) && response.patients != null) {
                adapter.setPatients(response.patients);
            }
        });

        viewModel.getActionResult().observe(this, res -> {
            if ("success".equals(res.status)) {
                Toast.makeText(this, res.message != null ? res.message : "Action completed", Toast.LENGTH_SHORT).show();
                if (userId != null) {
                    viewModel.loadPatients(userId);
                }
            } else {
                Toast.makeText(this, res.message != null ? res.message : "Error", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(this, err -> {
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        });

        binding.btnAddPatient.setOnClickListener(v -> {
            startActivity(new Intent(PatientsActivity.this, PatientFormActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null && viewModel != null) {
            viewModel.loadPatients(userId);
        }
    }
}
