package com.example.p2pnetwork;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    private TextView chatMessages;
    private EditText messageInput;
    private Button sendButton;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatMessages = findViewById(R.id.chatMessages);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> sendMessage());

        String ip = getIntent().getStringExtra("ip");
        int port = getIntent().getIntExtra("port", -1);

        new ChatTask().execute(ip, port);
    }

    private class ChatTask extends AsyncTask<Object, String, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            try {
                String ip = (String) params[0];
                int port = (int) params[1];

                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                publishProgress("Connected to " + ip + ":" + port);

                String message;
                while ((message = in.readLine()) != null) {
                    publishProgress("Friend: " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            chatMessages.append(values[0] + "\n");
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().toString();
        if (!message.isEmpty() && out != null) {
            out.println(message);
            chatMessages.append("You: " + message + "\n");
            messageInput.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
