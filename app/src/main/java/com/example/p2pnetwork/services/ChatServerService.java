package com.example.p2pnetwork.services;

import java.net.InetAddress;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.p2pnetwork.activities.ChatActivity;
import com.example.p2pnetwork.models.Message;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServerService {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private int port;
    private MessageListener messageListener;
    private Context context;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public ChatServerService(Context context, int port) {
        this.context = context;
        this.port = port;
    }

    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"));
                Log.d("ChatServerService", "Server started on port " + port);

                while (true) {
                    clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    Log.d("ChatServerService", "Client connected from " + clientAddress);

                    input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    output = new PrintWriter(clientSocket.getOutputStream(), true);

                    // Get the server's IP address
                    String serverAddress = serverSocket.getInetAddress().getHostAddress();

                    // Notify the MainActivity that the connection is established and start ChatActivity
                    mainHandler.post(() -> {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("ipAddress", serverAddress);
                        intent.putExtra("port", port);
                        context.startActivity(intent);
                    });

                    listenForMessages();
                }
            } catch (IOException e) {
                Log.e("ChatServerService", "Error starting server", e);
            }
        }).start();
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                String messageJson;
                while ((messageJson = input.readLine()) != null) {
                    Log.d("ChatServerService", "Received message: " + messageJson);
                    if (messageListener != null) {
                        Message message = new Gson().fromJson(messageJson, Message.class);
                        messageListener.onMessageReceived(message);
                    }
                }
            } catch (IOException e) {
                Log.e("ChatServerService", "Error listening for messages", e);
            }
        }).start();
    }

    public void stop() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e("ChatServerService", "Error closing socket", e);
        }
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public interface MessageListener {
        void onMessageReceived(Message message);
    }
}
