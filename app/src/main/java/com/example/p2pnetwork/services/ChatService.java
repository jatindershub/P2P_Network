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
import com.google.gson.Gson;
import com.example.p2pnetwork.models.Message;

public class ChatService {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String ipAddress;
    private int port;
    private String nodeId;
    private MessageListener messageListener;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Gson gson = new Gson();
    private Context context;

    public ChatService(Context context, String nodeId, String ipAddress, int port) {
        this.context = context;
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void start() {
        new Thread(() -> {
            try {
                socket = new Socket(ipAddress, port); // Establishing the TCP connection
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                Log.d("ChatService", "TCP connection established with " + ipAddress + ":" + port);


                // Start listening for messages
                listenForMessages();

                // Send broadcast indicating chat has started
                Intent broadcastIntent = new Intent("CHAT_REQUEST");
                broadcastIntent.putExtra("nodeId", nodeId);
                broadcastIntent.putExtra("ip", ipAddress);
                broadcastIntent.putExtra("port", port);
                context.sendBroadcast(broadcastIntent);
            } catch (IOException e) {
                Log.e("ChatService", "Error establishing TCP connection", e);
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
                    mainHandler.post(() -> messageListener.onMessageReceived(message));
                }
                // Send a broadcast when a new message is received
                Intent broadcastIntent = new Intent("CHAT_REQUEST");
                broadcastIntent.putExtra("nodeId", nodeId);
                broadcastIntent.putExtra("ip", ipAddress);
                broadcastIntent.putExtra("port", port);
                broadcastIntent.putExtra("message", messageJson); // Include the message JSON
                context.sendBroadcast(broadcastIntent);
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
            e.printStackTrace();
        }
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public interface MessageListener {
        void onMessageReceived(Message message);
    }
}
