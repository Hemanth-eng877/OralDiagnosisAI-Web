package com.oraldiagnosis.ai.ui.chatbot;

public class ChatMessage {
    public String sender;
    public String content;

    public ChatMessage(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }
}
