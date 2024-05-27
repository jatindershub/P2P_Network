package com.example.p2pnetwork;

import android.util.Log;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class MulticastService extends Thread {
    private static final String TAG = "MulticastService";

    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    private static final int PORT = 5000; // The port for the multicast group
    private MulticastSocket socket;
    private InetAddress group;
    private List<NodeInfo> nodeList = new CopyOnWriteArrayList<>();
    private Consumer<List<NodeInfo>> nodeListUpdater;
    private int dynamicPort;
    private volatile boolean running = true;
    private ChordNode localNode;

    public MulticastService(ChordNode localNode, Consumer<List<NodeInfo>> nodeListUpdater, int dynamicPort) {
        this.localNode = localNode;
        this.nodeListUpdater = nodeListUpdater;
        this.dynamicPort = dynamicPort;
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
            while (running) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                handleReceivedMessage(received);
            }
            socket.leaveGroup(group);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedMessage(String message) {
        Log.d(TAG, "Received message: " + message);
        try {
            String[] parts = message.split(",");
            if (parts.length == 4) {
                String action = parts[0];
                BigInteger nodeId = new BigInteger(parts[1]);
                InetAddress ip = InetAddress.getByName(parts[2]);
                int port = Integer.parseInt(parts[3]);

                if ("LEAVE".equals(action)) {
                    nodeList.removeIf(node -> node.getNodeId().equals(nodeId));
                    nodeListUpdater.accept(new ArrayList<>(nodeList));
                } else if ("JOIN".equals(action)) {
                    NodeInfo discoveredNode = new NodeInfo(nodeId, ip, port);

                    boolean nodeExists = false;
                    for (NodeInfo node : nodeList) {
                        if (node.getNodeId().equals(discoveredNode.getNodeId())) {
                            nodeExists = true;
                            break;
                        }
                    }

                    if (!nodeExists) {
                        nodeList.add(discoveredNode);
                        nodeListUpdater.accept(new ArrayList<>(nodeList));
                    }

                    // Update finger table with new node
                    if (localNode != null) {
                        localNode.updateFingerTable(new ChordNode(ip, nodeId, port));
                    }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendMulticastMessage(String message) {
        new Thread(() -> {
            try {
                byte[] buf = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopService() {
        running = false;
        interrupt();
    }
}
