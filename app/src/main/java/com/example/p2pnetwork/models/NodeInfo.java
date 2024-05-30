package com.example.p2pnetwork.models;

import java.math.BigInteger;
import java.net.InetAddress;

public class NodeInfo {
    private BigInteger nodeId;
    private InetAddress ip;
    private int port;

    public NodeInfo(BigInteger nodeId, InetAddress ip, int port) {
        this.nodeId = nodeId;
        this.ip = ip;
        this.port = port;
    }

    public BigInteger getNodeId() {
        return nodeId;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}

