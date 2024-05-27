package com.example.p2pnetwork;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastService extends Thread {
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    private static final int PORT = 5000;
    private MulticastSocket socket;
    private InetAddress group;
    private ChordNode localNode;

    public MulticastService(ChordNode localNode) {
        this.localNode = localNode;
        try {
            socket = new MulticastSocket(PORT);
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                handleReceivedMessage(received);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedMessage(String message) {
        // Parse the received message and update the Chord node information
        String[] parts = message.split(",");
        if (parts.length == 2) {
            try {
                BigInteger nodeId = new BigInteger(parts[0]);
                InetAddress ip = InetAddress.getByName(parts[1]);
                ChordNode discoveredNode = new ChordNode(ip);
                discoveredNode.setNodeId(nodeId);

                // Update the local node's finger table, predecessor, and successor
                localNode.updateFingerTable(discoveredNode);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMulticastMessage(String message) {
        try {
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

