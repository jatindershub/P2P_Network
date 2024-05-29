package com.example.p2pnetwork;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {
    private TextView chatMessages;
    private Socket socket;
    private BufferedReader input;
    private OutputStreamWriter output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatMessages = findViewById(R.id.chatMessages);

        String ipAddress = getIntent().getStringExtra("ipAddress");
        int port = getIntent().getIntExtra("port", -1);

        new Thread(() -> {
            try {
                // Establish the TCP connection
                socket = new Socket(ipAddress, port);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new OutputStreamWriter(socket.getOutputStream());

                // Listen for messages
                String message;
                while ((message = input.readLine()) != null) {
                    final String finalMessage = message; // Make the variable effectively final
                    runOnUiThread(() -> chatMessages.append("Node: " + finalMessage + "\n"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send message
    private void sendMessage(String message) {
        new Thread(() -> {
            try {
                if (output != null) {
                    output.write(message + "\n");
                    output.flush();
                    runOnUiThread(() -> chatMessages.append("You: " + message + "\n"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
