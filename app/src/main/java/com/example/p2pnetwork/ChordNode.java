package com.example.p2pnetwork;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChordNode {
    private BigInteger nodeId;
    private InetAddress ip;
    private ChordNode[] fingerTable;
    private ChordNode predecessor;
    private static final int M = 160; // Typically 160 for SHA-1

    public ChordNode(InetAddress ip) {
        this.ip = ip;
        this.nodeId = generateNodeId(ip.getHostAddress());
        this.fingerTable = new ChordNode[M];
        this.predecessor = null;
    }

    private BigInteger generateNodeId(String ipAddress) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(ipAddress.getBytes());
            return new BigInteger(1, hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BigInteger getNodeId() {
        return nodeId;
    }

    public ChordNode getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(ChordNode predecessor) {
        this.predecessor = predecessor;
    }

    public ChordNode getSuccessor() {
        return fingerTable[0]; // Assuming the first entry in the finger table is the successor
    }

    public void setSuccessor(ChordNode successor) {
        fingerTable[0] = successor;
    }

    public String getFingerTableAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < M; i++) {
            if (fingerTable[i] != null) {
                sb.append("Finger ").append(i).append(": ").append(fingerTable[i].getNodeId()).append("\n");
            }
        }
        return sb.toString();
    }

    // Method to initialize the finger table
    public void initializeFingerTable(ChordNode existingNode) {
        ChordNode successorNode = existingNode.findSuccessor(this.nodeId);
        fingerTable[0] = successorNode;
        predecessor = successorNode.getPredecessor();
        successorNode.setPredecessor(this);

        for (int i = 0; i < M - 1; i++) {
            BigInteger start = nodeId.add(BigInteger.valueOf(2).pow(i));
            if (isInInterval(start, nodeId, fingerTable[i].getNodeId())) {
                fingerTable[i + 1] = fingerTable[i];
            } else {
                fingerTable[i + 1] = existingNode.findSuccessor(start);
            }
        }
    }

    private boolean isInInterval(BigInteger id, BigInteger start, BigInteger end) {
        if (start.compareTo(end) < 0) {
            return id.compareTo(start) > 0 && id.compareTo(end) <= 0;
        } else {
            return id.compareTo(start) > 0 || id.compareTo(end) <= 0;
        }
    }

    // Method to find the successor of a given id
    public ChordNode findSuccessor(BigInteger id) {
        if (this == null) {
            throw new IllegalStateException("The node is not initialized correctly.");
        }

        if (getSuccessor() == null) {
            throw new IllegalStateException("The successor is not initialized correctly.");
        }

        if (isInInterval(id, nodeId, getSuccessor().getNodeId()) || id.equals(getSuccessor().getNodeId())) {
            return getSuccessor();
        } else {
            ChordNode closestNode = closestPrecedingNode(id);
            return closestNode.findSuccessor(id);
        }
    }

    // Method to find the closest preceding node
    public ChordNode closestPrecedingNode(BigInteger id) {
        for (int i = M - 1; i >= 0; i--) {
            if (fingerTable[i] != null && isInInterval(fingerTable[i].getNodeId(), nodeId, id)) {
                return fingerTable[i];
            }
        }
        return this;
    }

    // Other Chord protocol methods...
}
