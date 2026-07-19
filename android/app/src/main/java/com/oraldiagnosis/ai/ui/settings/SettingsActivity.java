package com.oraldiagnosis.ai.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.oraldiagnosis.ai.api.ApiClient;
import com.oraldiagnosis.ai.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:5000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentUrl = prefs.getString("base_url", DEFAULT_BASE_URL);
        binding.etBaseUrl.setText(currentUrl);

        binding.btnSaveSettings.setOnClickListener(v -> {
            String newUrl = binding.etBaseUrl.getText().toString().trim();
            if (newUrl.isEmpty()) {
                newUrl = DEFAULT_BASE_URL;
            }
            if (!newUrl.endsWith("/")) {
                newUrl += "/";
            }
            prefs.edit().putString("base_url", newUrl).apply();
            ApiClient.resetClient();
            Toast.makeText(this, "Server URL saved: " + newUrl, Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.btnResetUrl.setOnClickListener(v -> {
            binding.etBaseUrl.setText(DEFAULT_BASE_URL);
            prefs.edit().putString("base_url", DEFAULT_BASE_URL).apply();
            ApiClient.resetClient();
            Toast.makeText(this, "Reset to default emulator address", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
