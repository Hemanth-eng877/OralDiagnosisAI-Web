package com.oraldiagnosis.ai.ui.diagnose;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.oraldiagnosis.ai.databinding.ActivityPredictionBinding;
import com.oraldiagnosis.ai.models.PatientItem;
import com.oraldiagnosis.ai.tflite.TFLiteHelper;
import com.oraldiagnosis.ai.viewmodels.PatientViewModel;
import com.oraldiagnosis.ai.viewmodels.PredictionViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PredictionActivity extends AppCompatActivity {
    private ActivityPredictionBinding binding;
    private PredictionViewModel predictionViewModel;
    private PatientViewModel patientViewModel;
    private Bitmap selectedBitmap = null;
    private File tempImageFile = null;
    private List<PatientItem> patientList = new ArrayList<>();
    private String userId;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        Glide.with(this).load(selectedBitmap).into(binding.ivPreview);
                        binding.btnPredict.setEnabled(true);
                        binding.btnUpload.setEnabled(false);
                        binding.tvResult.setVisibility(View.GONE);

                        tempImageFile = new File(getCacheDir(), "upload_oral_" + System.currentTimeMillis() + ".jpg");
                        FileOutputStream fos = new FileOutputStream(tempImageFile);
                        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPredictionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);

        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        patientViewModel.getPatientsData().observe(this, response -> {
            if ("success".equals(response.status) && response.patients != null) {
                patientList = response.patients;
                ArrayAdapter<PatientItem> spinnerAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        patientList
                );
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerPatients.setAdapter(spinnerAdapter);
            }
        });

        if (userId != null) {
            patientViewModel.loadPatients(userId);
        }

        binding.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        binding.btnPredict.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                binding.btnPredict.setEnabled(false);
                predictionViewModel.runLocalPrediction(selectedBitmap);
            }
        });

        binding.btnUpload.setOnClickListener(v -> {
            int pos = binding.spinnerPatients.getSelectedItemPosition();
            if (pos < 0 || pos >= patientList.size()) {
                Toast.makeText(this, "Please select a patient first.", Toast.LENGTH_SHORT).show();
                return;
            }
            PatientItem patient = patientList.get(pos);
            if (tempImageFile != null && tempImageFile.exists() && userId != null) {
                binding.btnUpload.setEnabled(false);
                predictionViewModel.uploadDiagnosisToServer(userId, patient.id, tempImageFile);
                Toast.makeText(this, "Uploading diagnosis...", Toast.LENGTH_SHORT).show();
            }
        });

        predictionViewModel.getLocalPredictionResult().observe(this, result -> {
            binding.btnPredict.setEnabled(true);
            binding.tvResult.setText(String.format("On-Device AI Diagnosis: %s\\nConfidence: %.2f%%", result.label, result.confidence));
            binding.tvResult.setVisibility(View.VISIBLE);
            binding.btnUpload.setEnabled(true);
        });

        predictionViewModel.getUploadResult().observe(this, response -> {
            binding.btnUpload.setEnabled(true);
            if ("success".equals(response.status)) {
                Toast.makeText(this, "Diagnosis saved to enterprise cloud database successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, response.message != null ? response.message : "Cloud upload failed", Toast.LENGTH_SHORT).show();
            }
        });

        predictionViewModel.getErrorMessage().observe(this, err -> {
            binding.btnPredict.setEnabled(true);
            binding.btnUpload.setEnabled(true);
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        });
    }
}
