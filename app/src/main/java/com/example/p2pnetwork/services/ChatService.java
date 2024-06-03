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

    public ChatService(Context context, String ipAddress, int port) {
        this.context = context;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void start() {
        new Thread(() -> {
            try {
                socket = new Socket(ipAddress, port); // Establishing the TCP connection
                Log.d("ChatService", "TCP connection established with " + ipAddress + ":" + port);

                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                // Get the client's IP address
                String clientAddress = socket.getLocalAddress().getHostAddress();
                Log.d("ChatService", "ClientAddress: " + clientAddress);

                // Notify the MainActivity that the connection is established and start ChatActivity
                mainHandler.post(() -> {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("ipAddress", clientAddress);
                    //intent.putExtra("ipAddress", "192.168.1.76");
                    intent.putExtra("port", port);
                    context.startActivity(intent);
                });

                // Start listening for messages
                listenForMessages();
            } catch (IOException e) {
                e.printStackTrace();
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

    public void sendMessage(Message message) {
        new Thread(() -> {
            if (output != null) {
                String messageJson = gson.toJson(message);
                output.println(messageJson); // Send message over TCP connection
                output.flush();
                Log.d("ChatService", "Sent message: " + messageJson);
            } else {
                Log.e("ChatService", "Output stream is null, message not sent");
            }
        }).start();
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
