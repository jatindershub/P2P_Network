package com.example.p2pnetwork.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.p2pnetwork.activities.ChatActivity;
import com.example.p2pnetwork.helper.EncryptionHelper;
import com.google.gson.Gson;
import com.example.p2pnetwork.models.Message;

public class ChatService {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String ipAddress;
    private int port;
    private MessageListener messageListener;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Gson gson = new Gson();
    private Context context;
    private boolean isReady = false;

    public ChatService(Context context, String ipAddress, int port) {
        this.context = context;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public synchronized void start() {
        new Thread(() -> {
            try {
                Log.d("ChatService", "Attempting to establish TCP connection with " + ipAddress + ":" + port);
                socket = new Socket(ipAddress, port); // Establishing the TCP connection
                Log.d("ChatService", "TCP connection established with " + ipAddress + ":" + port);

                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                // Set the flag to indicate readiness
                synchronized (this) {
                    isReady = true;
                    Log.d("ChatService", "Setting isReady to true");
                    notifyAll();
                }
                Log.d("ChatService", "Connection is ready");

                // Get the client's IP address
                String clientAddress = socket.getLocalAddress().getHostAddress();
                Log.d("ChatService", "ClientAddress: " + clientAddress);

                // Notify the MainActivity that the connection is established and start ChatActivity
                mainHandler.post(() -> {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("ipAddress", clientAddress);
                    intent.putExtra("port", port);
                    Log.d("ChatService", "Starting ChatActivity with IP: " + clientAddress + " and Port: " + port);
                    context.startActivity(intent);
                });

                // Start listening for messages
                listenForMessages();
            } catch (IOException e) {
                Log.e("ChatService", "Error establishing TCP connection", e);
                e.printStackTrace();
                synchronized (this) {
                    isReady = false;
                    notifyAll();
                }
            }
        }).start();
    }

    private void listenForMessages() {
        try {
            String messageJson;
            while ((messageJson = input.readLine()) != null) {
                Log.d("ChatService", "Received message: " + messageJson);
                if (messageListener != null) {
                    Message message = gson.fromJson(messageJson, Message.class);
                    Log.d("ChatService", "Passing message to listener: " + message.getMessage());
                    mainHandler.post(() -> messageListener.onMessageReceived(message));
                }
            }
        } catch (IOException e) {
            Log.e("ChatService", "Error listening for messages", e);
            e.printStackTrace();
        }
    }

    public void receiveMessage(String concatenatedMessage) {
        try {
            // Split the concatenated message into encrypted message, encrypted AES key, and hash
            String[] parts = concatenatedMessage.split(":");
            String encryptedMessage = parts[0];
            String encryptedAESKey = parts[1];
            String receivedHash = parts[2];

            // Decrypt the AES key using RSA private key
            EncryptionHelper.decryptAESKey(encryptedAESKey);

            // Decrypt the message using AES
            String decryptedMessage = EncryptionHelper.decryptMessage(encryptedMessage);

            // Verify the hash
            boolean isHashValid = EncryptionHelper.verifyHash(decryptedMessage, receivedHash);

            if (isHashValid) {
                // Process the message
                Message message = gson.fromJson(decryptedMessage, Message.class);
                // Handle the received message
            } else {
                Log.e("ChatService", "Message hash verification failed");
            }
        } catch (Exception e) {
            Log.e("ChatService", "Error receiving message: " + e.getMessage());
        }
    }


    public void sendMessage(Message message) {
        Log.d("ChatService", "sendMessage called");
        new Thread(() -> {
            Log.d("ChatService", "sendMessage thread started");
            waitForReady(); // todo: der er noget med WaitForReady()
            Log.d("ChatService", "waitForReady completed");
            if (output != null) {
                try {
                    // Initialize keys if not already done
                    EncryptionHelper.initializeKeys();

                    // Convert the message to JSON
                    String messageJson = gson.toJson(message);

                    // Encrypt the message using AES
                    String encryptedMessage = EncryptionHelper.encryptMessage(messageJson);

                    // Encrypt the AES key with RSA public key
                    String encryptedAESKey = EncryptionHelper.encryptAESKey();

                    // Create a SHA-256 hash of the original message
                    String messageHash = EncryptionHelper.createHash(messageJson);

                    // Concatenate the encrypted message, encrypted AES key, and the hash
                    String concatenatedMessage = encryptedMessage + ":" + encryptedAESKey + ":" + messageHash;

                    // Send the concatenated message over the TCP connection
                    output.println(concatenatedMessage);
                    output.flush();
                    Log.d("ChatService", "Sent message: " + concatenatedMessage);
                } catch (Exception e) {
                    Log.e("ChatService", "Error sending message: " + e.getMessage());
                }
            } else {
                Log.e("ChatService", "Output stream is null, message not sent");
            }
        }).start();
    }



    private synchronized void waitForReady() {
        while (!isReady) {
            try {
                Log.d("ChatService", "Waiting for readiness");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("ChatService", "Thread interrupted while waiting for readiness", e);
            }
        }
        Log.d("ChatService", "Service is ready");
    }

    public void stop() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e("ChatService", "Error closing socket", e);
            e.printStackTrace();
        }
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
        Log.d("ChatService", "MessageListener set");
    }

    public interface MessageListener {
        void onMessageReceived(Message message);
    }
}
