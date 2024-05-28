package com.example.p2pnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ChordNode node;
    private MulticastService multicastService;
    private StabilizationService stabilizationService;
    private TextView nodeStatus;
    private Button joinNetworkButton;
    private Button leaveNetworkButton;
    private Button viewDetailsButton;
    private RecyclerView nodesRecyclerView;
    private NodesAdapter nodesAdapter;
    private List<NodeInfo> nodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nodeStatus = findViewById(R.id.nodeStatus);
        joinNetworkButton = findViewById(R.id.joinNetworkButton);
        leaveNetworkButton = findViewById(R.id.leaveNetworkButton);
        viewDetailsButton = findViewById(R.id.viewDetailsButton);
        nodesRecyclerView = findViewById(R.id.nodesRecyclerView);

        EditText ipAddressInput = findViewById(R.id.ipAddressInput);
        EditText portInput = findViewById(R.id.portInput);
        Button startChatButton = findViewById(R.id.startChatButton);

        startChatButton.setOnClickListener(v -> {
            String ipAddress = ipAddressInput.getText().toString();
            int port = Integer.parseInt(portInput.getText().toString());

            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("ipAddress", ipAddress);
            intent.putExtra("port", port);
            startActivity(intent);
        });

        nodeList = new ArrayList<>();
        nodesAdapter = new NodesAdapter(nodeList);
        nodesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nodesRecyclerView.setAdapter(nodesAdapter);

        startMulticastService();

        joinNetworkButton.setOnClickListener(v -> new Thread(this::joinNetwork).start());
        leaveNetworkButton.setOnClickListener(v -> new Thread(this::leaveNetwork).start());
        viewDetailsButton.setOnClickListener(v -> viewNodeDetails());
    }

    private void startMulticastService() {
        InetAddress ip = NetworkUtils.getIPAddress();
        int port = NetworkUtils.getAvailablePort();

        if (ip != null && port != -1) {
            multicastService = MulticastService.getInstance(null, this::updateNodeList, port);
            multicastService.start();
        } else {
            runOnUiThread(() -> nodeStatus.setText("Failed to get IP address or port"));
        }
    }

    private void startTcpServer() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(node.getDynamicPort());
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStreamWriter output = new OutputStreamWriter(clientSocket.getOutputStream());

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "New chat request", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("ipAddress", clientSocket.getInetAddress().getHostAddress());
                    intent.putExtra("port", clientSocket.getPort());
                    startActivity(intent);
                });

                String message;
                while ((message = input.readLine()) != null) {
                    // Handle incoming message
                    // Update UI or notify user
                }

                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    private void joinNetwork() {
        new Thread(() -> {
            try {
                InetAddress ip = NetworkUtils.getIPAddress();
                int port = NetworkUtils.getAvailablePort();

                if (ip != null && port != -1) {
                    node = new ChordNode(ip, port);

                    // Try to find an existing node to join the network
                    if (!nodeList.isEmpty()) {
                        NodeInfo bootstrapNodeInfo = nodeList.get(0); // Assuming the first node in the list as bootstrap
                        ChordNode bootstrapNode = new ChordNode(bootstrapNodeInfo);

                        ChordNode successor = bootstrapNode.findSuccessor(node.getNodeId());
                        node.setSuccessor(successor);
                        node.setPredecessor(successor.getPredecessor());
                        successor.setPredecessor(node);
                    } else {
                        // This is the first node in the network
                        node.setSuccessor(node);
                        node.setPredecessor(node);
                    }

                    // Update multicastService to use the new node
                    multicastService.setLocalNode(node);

                    stabilizationService = new StabilizationService(node, multicastService);
                    stabilizationService.start();

                    runOnUiThread(() -> {
                        nodeStatus.setText("Node ID: " + node.getNodeId());
                        viewDetailsButton.setEnabled(true);
                        leaveNetworkButton.setEnabled(true);
                        joinNetworkButton.setEnabled(false);
                    });

                    multicastService.sendMulticastMessage("JOIN," + node.getNodeId() + "," + ip.getHostAddress() + "," + port);
                } else {
                    runOnUiThread(() -> nodeStatus.setText("Failed to get IP address or port"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void leaveNetwork() {
        if (multicastService != null && node != null) {
            multicastService.sendMulticastMessage("LEAVE," + node.getNodeId() + "," + node.getIp().getHostAddress() + "," + node.getDynamicPort());
            stabilizationService.stopService();
            runOnUiThread(() -> {
                nodeStatus.setText("Node has left the network.");
                viewDetailsButton.setEnabled(false);
                leaveNetworkButton.setEnabled(false);
                joinNetworkButton.setEnabled(true);
            });
        }
    }

    private void viewNodeDetails() {
        Intent intent = new Intent(MainActivity.this, NodeDetailsActivity.class);
        intent.putExtra("nodeId", node.getNodeId().toString());
        intent.putExtra("predecessorId", node.getPredecessor() != null ? node.getPredecessor().getNodeId().toString() : "None");
        intent.putExtra("successorId", node.getSuccessor() != null ? node.getSuccessor().getNodeId().toString() : "None");
        intent.putExtra("fingerTable", node.getFingerTableAsStringArray());
        startActivity(intent);
    }

    private void updateNodeList(List<NodeInfo> newNodeList) {
        runOnUiThread(() -> {
            nodeList.clear();
            nodeList.addAll(newNodeList);
            nodesAdapter.notifyDataSetChanged();
        });
    }
}
