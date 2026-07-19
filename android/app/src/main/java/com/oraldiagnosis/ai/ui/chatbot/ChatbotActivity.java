package com.oraldiagnosis.ai.ui.chatbot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.oraldiagnosis.ai.databinding.ActivityChatbotBinding;

public class ChatbotActivity extends AppCompatActivity {
    private ActivityChatbotBinding binding;
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatbotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);

        adapter.addMessage(new ChatMessage("AI Assistant", "Hello Doctor! I am the OralDiagnosisAI Clinical Assistant. Ask me about OSCC staging, biopsy criteria, or image capture guidelines."));

        binding.btnSend.setOnClickListener(v -> {
            String query = binding.etMessage.getText().toString().trim();
            if (query.isEmpty()) return;

            adapter.addMessage(new ChatMessage("Doctor", query));
            binding.etMessage.setText("");

            String response = generateAiAnswer(query);
            adapter.addMessage(new ChatMessage("AI Assistant", response));
            binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
        });
    }

    private String generateAiAnswer(String query) {
        String q = query.toLowerCase();
        if (q.contains("oscc") || q.contains("cancer") || q.contains("carcinoma")) {
            return "Oral Squamous Cell Carcinoma (OSCC) accounts for >90% of oral cancers. Early lesions often present as leukoplakia, erythroplakia, or non-healing mucosal ulcers. Our TFLite model provides instant on-device screening assistance.";
        } else if (q.contains("image") || q.contains("photo") || q.contains("capture") || q.contains("camera")) {
            return "For accurate screening, ensure bright lighting, focus clearly on the oral lesion or mucosa, and avoid motion blur or obstructing dental instruments.";
        } else if (q.contains("accuracy") || q.contains("confidence") || q.contains("model")) {
            return "The on-device model (`oral_model.tflite`) analyzes 224x224 RGB image tensors using deep convolutional feature extraction. Confidence > 80% warrants clinical follow-up.";
        } else {
            return "Thank you for your question. As an AI screening assistant, I recommend clinical correlation and histopathological examination for all suspicious oral mucosal lesions.";
        }
    }
}
