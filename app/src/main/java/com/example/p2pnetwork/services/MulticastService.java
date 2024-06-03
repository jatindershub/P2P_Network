package com.example.p2pnetwork.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.p2pnetwork.activities.ChatActivity;
import com.example.p2pnetwork.models.NodeInfo;
import com.example.p2pnetwork.network.ChordNode;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class MulticastService extends Thread {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String TAG = "MulticastService";
    private static MulticastService instance;
    private MulticastSocket socket;
    private InetAddress group;
    private ChordNode localNode;
    private Consumer<List<NodeInfo>> nodeListUpdater;
    private List<NodeInfo> nodeList;
    private int dynamicPort;
    private Context context;
    private static final int PORT = 5000;
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    public MulticastService(ChordNode localNode, Consumer<List<NodeInfo>> nodeListUpdater, int port) {
        this.localNode = localNode;
        this.nodeListUpdater = nodeListUpdater;
        this.dynamicPort = port;
        nodeList = new ArrayList<>();
        try {
            socket = new MulticastSocket(PORT);
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized MulticastService getInstance(ChordNode localNode, Consumer<List<NodeInfo>> nodeListUpdater, int port) {
        if (instance == null) {
            instance = new MulticastService(localNode, nodeListUpdater, port);
        }
        return instance;
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
        //Log.d(TAG, "Received message: " + message);
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
        executor.execute(() -> {
            try {
                byte[] buf = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
