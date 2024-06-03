package com.example.p2pnetwork.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p2pnetwork.R;
import com.example.p2pnetwork.models.Message;
import com.example.p2pnetwork.services.ChatServerService;
import com.example.p2pnetwork.services.ChatService;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private TextView chatMessages;
    private EditText messageInput;
    private Button sendButton;
    private ChatService chatService;
    private ChatServerService chatServerService;
    private String ipAddress;
    private String nodeId;
    private Gson gson = new Gson();
    private boolean isServer;

    private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String messageJson = intent.getStringExtra("message");
            Log.d("ChatActivity", "Broadcast received with message: " + messageJson);
            Message message = gson.fromJson(messageJson, Message.class);
            runOnUiThread(() -> {
                Log.d("ChatActivity", "Updating UI with message: " + message.getMessage());
                chatMessages.append("Node [" + message.getIpAddress() + "]: " + message.getMessage() + "\n");
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatMessages = findViewById(R.id.chatMessages);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        ipAddress = getIntent().getStringExtra("ip");
        int port = getIntent().getIntExtra("port", -1);
        nodeId = getIntent().getStringExtra("nodeId");
        isServer = getIntent().getBooleanExtra("isServer", false);


        // Initialize and start the ChatService
        chatService = new ChatService(ipAddress, port);
        chatService.start();

        if (isServer) {
            chatServerService = new ChatServerService(port);
            chatServerService.start();
            chatServerService.setMessageListener(new ChatServerService.MessageListener() {
                @Override
                public void onMessageReceived(Message message) {
                    runOnUiThread(() -> {
                        Log.d("ChatActivity", "Updating UI with message: " + message.getMessage());
                        chatMessages.append("Node [" + message.getIpAddress() + "]: " + message.getMessage() + "\n");
                    });
                }
            });
        } else {
            chatService = new ChatService(ipAddress, port);  // Pass the context and node ID here
            chatService.start();
            chatService.setMessageListener(new ChatService.MessageListener() {
                @Override
                public void onMessageReceived(Message message) {
                    runOnUiThread(() -> {
                        Log.d("ChatActivity", "Updating UI with message: " + message.getMessage());
                        chatMessages.append("Node [" + message.getIpAddress() + "]: " + message.getMessage() + "\n");
                    });
                }
            });
        }
            sendButton.setOnClickListener(v -> {
                String messageText = messageInput.getText().toString();
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                Message message = new Message(messageText, timestamp, ipAddress);
                if (isServer) {
                    chatServerService.sendMessage(message);
                } else {
                    chatService.sendMessage(message);
                }
                chatMessages.append("Me: " + message.getMessage() + " [" + message.getTimestamp() + "]\n");
                messageInput.setText("");
                Log.d("ChatActivity", "Sent message: " + message.getMessage());
            });

            // Register the BroadcastReceiver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(chatMessageReceiver, new IntentFilter("CHAT_REQUEST"), Context.RECEIVER_NOT_EXPORTED);
            }
            Log.d("ChatActivity", "BroadcastReceiver registered");
        }

        @Override
        protected void onDestroy () {
            super.onDestroy();
            if (isServer) {
                chatServerService.stop();
            } else {
                chatService.stop();
            }

            unregisterReceiver(chatMessageReceiver); // Unregister the BroadcastReceiver
            Log.d("ChatActivity", "BroadcastReceiver unregistered");
        }

    }