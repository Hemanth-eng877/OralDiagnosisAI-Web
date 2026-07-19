package com.oraldiagnosis.ai.ui.register;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.oraldiagnosis.ai.databinding.ActivityRegisterBinding;
import com.oraldiagnosis.ai.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnSignup.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!email.contains("@")) {
                Toast.makeText(this, "Enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.btnSignup.setEnabled(false);
            viewModel.signup(name, email, password);
        });

        binding.btnBackToLogin.setOnClickListener(v -> finish());

        viewModel.getSignupResult().observe(this, authResponse -> {
            binding.btnSignup.setEnabled(true);
            if ("success".equals(authResponse.status)) {
                Toast.makeText(this, "Account created successfully! Please log in.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, authResponse.message != null ? authResponse.message : "Registration error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
