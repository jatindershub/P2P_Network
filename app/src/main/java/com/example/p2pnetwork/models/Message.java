package com.example.p2pnetwork.models;

import java.io.Serializable;

public class Message implements Serializable {
    private String message;
    private String timestamp;
    private String ipAddress;

    public Message(String message, String timestamp, String ipAddress) {
        this.message = message;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}

