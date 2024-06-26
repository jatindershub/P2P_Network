package com.example.p2pnetwork.network;

import com.example.p2pnetwork.models.NodeInfo;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChordNode {
    private static final String TAG = "ChordNode";
    private int tcpPort;
    private BigInteger nodeId;
    private InetAddress ip;
    private int dynamicPort;
    public static final int M = 160; // Typically 160 for SHA-1
    private ChordNode[] fingerTable;
    private ChordNode predecessor;

    public ChordNode(NodeInfo nodeInfo) {
        this(nodeInfo.getIp(), nodeInfo.getPort());
        this.nodeId = nodeInfo.getNodeId();
    }

    public ChordNode(InetAddress ip, int dynamicPort) {
        this.ip = ip;
        this.nodeId = generateNodeId(ip.getHostAddress());
        this.dynamicPort = dynamicPort;
        this.fingerTable = new ChordNode[M];
        this.predecessor = null;
    }

    public ChordNode(InetAddress ip, BigInteger nodeId, int dynamicPort) {
        this.ip = ip;
        this.nodeId = nodeId;
        this.dynamicPort = dynamicPort;
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

    public InetAddress getIp() {
        return ip;
    }

    public int getDynamicPort() {
        return dynamicPort;
    }

    public ChordNode getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(ChordNode predecessor) {
        this.predecessor = predecessor;
        //Log.d(TAG, "Predecessor set to: " + (predecessor != null ? predecessor.getNodeId() : "None"));
    }

    public ChordNode getSuccessor() {
        return fingerTable[0]; // Assuming the first entry in the finger table is the successor
    }

    public void setSuccessor(ChordNode successor) {
        fingerTable[0] = successor;
        //Log.d(TAG, "Successor set to: " + (successor != null ? successor.getNodeId() : "None"));
    }

    public void updateFingerTable(ChordNode newNode) {
        BigInteger newId = newNode.getNodeId();
        for (int i = 0; i < M; i++) {
            BigInteger start = nodeId.add(BigInteger.valueOf(2).pow(i)).mod(BigInteger.valueOf(2).pow(M));

            // Check if the current finger table entry is null or needs to be updated
            if (fingerTable[i] == null || isInInterval(start, nodeId, fingerTable[i].getNodeId())) {
                ChordNode successorNode = findSuccessor(start);

                // Only update if the new node is different from the current finger
                if (successorNode != null && !successorNode.equals(this) && !successorNode.equals(fingerTable[i])) {
                    fingerTable[i] = successorNode;
                    //Log.d(TAG, "Finger table updated at " + i + " with node " + successorNode.getNodeId());
                }
            }
        }

        // Update the successor if necessary
        if (getSuccessor() == null || isInInterval(newNode.getNodeId(), nodeId, getSuccessor().getNodeId())) {
            setSuccessor(newNode);
            //Log.d(TAG, "Successor set to: " + newNode.getNodeId());
        }

        // Update the predecessor if necessary
        if (predecessor == null || isInInterval(newNode.getNodeId(), predecessor.getNodeId(), nodeId)) {
            setPredecessor(newNode);
            //Log.d(TAG, "Predecessor set to: " + newNode.getNodeId());
        }
    }

    private boolean isInInterval(BigInteger id, BigInteger start, BigInteger end) {
        if (start.compareTo(end) < 0) {
            return id.compareTo(start) > 0 && id.compareTo(end) <= 0;
        } else {
            return id.compareTo(start) > 0 || id.compareTo(end) <= 0;
        }
    }

    public String[] getFingerTableAsStringArray() {
        String[] fingerTableArray = new String[M];
        for (int i = 0; i < M; i++) {
            fingerTableArray[i] = "Finger " + i + ": " + (fingerTable[i] != null ? fingerTable[i].getNodeId() : "None");
        }
        return fingerTableArray;
    }

    public ChordNode findSuccessor(BigInteger id) {
        if (getSuccessor() != null && isInInterval(id, nodeId, getSuccessor().getNodeId())) {
            return getSuccessor();
        } else {
            ChordNode closestNode = closestPrecedingNode(id);
            if (closestNode == this) {
                return this;
            }
            return closestNode.findSuccessor(id);
        }
    }

    public ChordNode closestPrecedingNode(BigInteger id) {
        for (int i = M - 1; i >= 0; i--) {
            if (fingerTable[i] != null && isInInterval(fingerTable[i].getNodeId(), nodeId, id)) {
                return fingerTable[i];
            }
        }
        return this;
    }
}