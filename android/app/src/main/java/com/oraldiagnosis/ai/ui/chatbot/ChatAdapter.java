package com.oraldiagnosis.ai.ui.chatbot;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oraldiagnosis.ai.databinding.ItemChatMessageBinding;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> messageList = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(messageList.get(position));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageBinding binding;

        public ChatViewHolder(ItemChatMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessage msg) {
            binding.tvSender.setText(msg.sender);
            binding.tvContent.setText(msg.content);
            if ("Doctor".equalsIgnoreCase(msg.sender)) {
                binding.tvSender.setTextColor(0xFF1E3A8A);
            } else {
                binding.tvSender.setTextColor(0xFF10B981);
            }
        }
    }
}
