package com.example.p2pnetwork.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p2pnetwork.R;
import com.example.p2pnetwork.models.Message;
import com.example.p2pnetwork.services.ChatService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private TextView chatMessages;
    private EditText messageInput;
    private Button sendButton;
    private ChatService chatService;
    private String ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatMessages = findViewById(R.id.chatMessages);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        ipAddress = getIntent().getStringExtra("ipAddress");
        int port = getIntent().getIntExtra("port", -1);

        // Initialize and start the ChatService
        chatService = new ChatService(ipAddress, port);
        chatService.start();

        // Update the UI when a new message is received
        chatService.setMessageListener(new ChatService.MessageListener() {
            @Override
            public void onMessageReceived(com.example.p2pnetwork.models.Message message) {  // Ensure this uses your Message class
                runOnUiThread(() -> chatMessages.append("Node [" + message.getIpAddress() + "]: " + message.getMessage() + "\n"));
            }
        });

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            Message message = new Message(messageText, timestamp, ipAddress);
            chatService.sendMessage(message);
            chatMessages.append("Me: " + message.getMessage() + " [" + message.getTimestamp() + "]\n");
            messageInput.setText("");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatService.stop();
    }
}