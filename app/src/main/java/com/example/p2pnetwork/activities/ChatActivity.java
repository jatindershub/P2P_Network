package com.example.p2pnetwork.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.p2pnetwork.R;
import com.example.p2pnetwork.models.Message;
import com.example.p2pnetwork.services.ChatService;

public class ChatActivity extends AppCompatActivity {
    private TextView chatMessages;
    private EditText messageInput;
    private Button sendButton;
    private ChatService chatService;
    private String ipAddress;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatMessages = findViewById(R.id.chatMessages);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        ipAddress = getIntent().getStringExtra("ipAddress");
        port = getIntent().getIntExtra("port", -1);

        // Initialize and start the ChatService
        chatService = new ChatService(this, ipAddress, port);
        chatService.setMessageListener(new ChatService.MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                runOnUiThread(() -> {
                    String messageText = "Node [" + message.getIpAddress() + "]: " + message.getMessage() + "\n";
                    chatMessages.append(messageText);
                    Log.d("ChatActivity", "Updating UI with message: " + messageText);
                });
            }
        });

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString();
            String timestamp = String.valueOf(System.currentTimeMillis());
            Message message = new Message(messageText, timestamp, ipAddress);

            Log.d("ChatActivity", "Sending message: " + message.getMessage());
            chatService.sendMessage(message);
            chatMessages.append("Me: " + message.getMessage() + " [" + message.getTimestamp() + "]\n");
            messageInput.setText("");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatService != null) {
            chatService.stop();
        }
    }
}