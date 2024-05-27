package com.example.p2pnetwork;

import java.io.*;
import java.net.Socket;

public class TcpClient {
    private Socket socket;
    private PrintWriter out;

    public TcpClient(String ipAddress, int port) throws IOException {
        this.socket = new Socket(ipAddress, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void close() throws IOException {
        out.close();
        socket.close();
    }
}
