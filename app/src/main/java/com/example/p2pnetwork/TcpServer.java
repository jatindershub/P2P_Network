package com.example.p2pnetwork;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer extends Thread {
    private int port;
    private MessageHandler messageHandler;

    public TcpServer(int port, MessageHandler messageHandler) {
        this.port = port;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, messageHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private MessageHandler messageHandler;

        public ClientHandler(Socket socket, MessageHandler messageHandler) {
            this.clientSocket = socket;
            this.messageHandler = messageHandler;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String message;
                while ((message = in.readLine()) != null) {
                    messageHandler.handleMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface MessageHandler {
        void handleMessage(String message);
    }
}

