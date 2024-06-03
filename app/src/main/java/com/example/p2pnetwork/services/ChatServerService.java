package com.example.p2pnetwork.services;

import java.net.InetAddress;
import android.util.Log;

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
    private String ipAddress;
    private MessageListener messageListener;

    public ChatServerService(int port) {
        this.port = port;
    }

    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"));
                Log.d("ChatServerService", "Server started on port " + port);

                // Accept incoming connections
                clientSocket = serverSocket.accept();
                Log.d("ChatServerService", "Client connected from " + clientSocket.getInetAddress());

                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                output = new PrintWriter(clientSocket.getOutputStream(), true);

                // Start listening for messages
                listenForMessages();
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

    public void sendMessage(Message message) {
        new Thread(() -> {
            if (output != null) {
                String messageJson = new Gson().toJson(message);
                output.println(messageJson);
                output.flush();
                Log.d("ChatServerService", "Sent message: " + messageJson);
            } else {
                Log.e("ChatServerService", "Output stream is null, message not sent");
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
