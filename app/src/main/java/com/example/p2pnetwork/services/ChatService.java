package com.example.p2pnetwork.services;

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
    private MessageListener messageListener;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Gson gson = new Gson();

    public ChatService(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void start() {
        new Thread(() -> {
            try {
                socket = new Socket(ipAddress, port);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

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
                    mainHandler.post(() -> messageListener.onMessageReceived(message));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) {
        new Thread(() -> {
            if (output != null) {
                String messageJson = gson.toJson(message);
                output.println(message);
                output.flush();
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
