package com.example.p2pnetwork.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p2pnetwork.R;
import com.example.p2pnetwork.adapters.NodesAdapter;
import com.example.p2pnetwork.models.Message;
import com.example.p2pnetwork.models.NodeInfo;
import com.example.p2pnetwork.network.ChordNode;
import com.example.p2pnetwork.network.NetworkUtils;
import com.example.p2pnetwork.services.ChatServerService;
import com.example.p2pnetwork.services.ChatService;
import com.example.p2pnetwork.services.MulticastService;
import com.example.p2pnetwork.services.StabilizationService;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ChordNode node;
    private MulticastService multicastService;
    private StabilizationService stabilizationService;
    private TextView nodeStatus;
    private Button joinNetworkButton;
    private Button leaveNetworkButton;
    private RecyclerView nodesRecyclerView;
    private NodesAdapter nodesAdapter;
    private List<NodeInfo> nodeList;
    private EditText ipAddressInput;
    private EditText portInput;
    private Button startChatButton;
    private Button viewDetailsButton;
    private ChatService chatService;
    private ChatServerService chatServerService;
    private static final int SERVER_PORT = 59342;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        nodeStatus = findViewById(R.id.nodeStatus);
        joinNetworkButton = findViewById(R.id.joinNetworkButton);
        leaveNetworkButton = findViewById(R.id.leaveNetworkButton);
        viewDetailsButton = findViewById(R.id.viewDetailsButton);
        nodesRecyclerView = findViewById(R.id.nodesRecyclerView);
        ipAddressInput = findViewById(R.id.ipAddressInput);
        portInput = findViewById(R.id.portInput);
        nodesRecyclerView = findViewById(R.id.nodesRecyclerView);
        startChatButton = findViewById(R.id.startChatButton);

        // Start the TCP server
        startServer();

        // Set up the button click listener
        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = ipAddressInput.getText().toString();
                int port = Integer.parseInt(portInput.getText().toString());

                // Connect to the server (self or other device)
                chatService = new ChatService(MainActivity.this, ipAddress, port);
                chatService.start();
                chatService.setMessageListener(new ChatService.MessageListener() {
                    @Override
                    public void onMessageReceived(Message message) {
                        // todo: handle receuved message
                        Log.d("MainActivity", "Received message: " + message.getMessage());
                    }
                });
            }
        });

        nodeList = new ArrayList<>();
        nodesAdapter = new NodesAdapter(nodeList);
        nodesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nodesRecyclerView.setAdapter(nodesAdapter);

        // This sets an item click listener on the nodesAdapter.
        // The setOnItemClickListener method is a method defined in the NodesAdapter class,
        // which allows you to handle click events on items in the RecyclerView.
        nodesAdapter.setOnItemClickListener(nodeInfo -> {
            ipAddressInput.setText(nodeInfo.getIp().getHostAddress());
            portInput.setText(String.valueOf(nodeInfo.getPort()));
        });

        // Starts the node discovery mechanism
        startMulticastService();

        // Other button click listener
        joinNetworkButton.setOnClickListener(v -> new Thread(this::joinNetwork).start());
        leaveNetworkButton.setOnClickListener(v -> new Thread(this::leaveNetwork).start());
        viewDetailsButton.setOnClickListener(v -> viewNodeDetails());
    }

    private void startServer() {
        chatServerService = new ChatServerService(MainActivity.this, SERVER_PORT);
        chatServerService.start();
        chatServerService.setMessageListener(new ChatServerService.MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                // Handle received message
                Log.d("MainActivity", "Received message: " + message.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatServerService != null) {
            chatServerService.stop();
        }
        if (chatService != null) {
            chatService.stop();
        }
    }

    private void startMulticastService() {
        InetAddress ip = NetworkUtils.getIPAddress();
        //int port = NetworkUtils.getAvailablePort();
        int port = SERVER_PORT;

        // Conditional statement checks if the IP and port are valid
        if (ip != null && port != -1) {
            multicastService = MulticastService.getInstance(null, this::updateNodeList, port);
            multicastService.start();
        } else {
            runOnUiThread(() -> nodeStatus.setText("Failed to get IP address or port"));
        }
    }

    private void joinNetwork() {
        new Thread(() -> {
            try {
                InetAddress ip = NetworkUtils.getIPAddress();
                //int port = NetworkUtils.getAvailablePort();
                int port = SERVER_PORT;

                if (ip != null && port != -1) {
                    node = new ChordNode(ip, port);
                    Log.d("joinNetwork", "Node initialized with IP: " + ip + " and port: " + port);


                    // Try to find an existing node to join the network
                    if (!nodeList.isEmpty()) {
                        NodeInfo anchorNodeInfo = nodeList.get(0); // Assuming the first node in the list as anchor
                        ChordNode anchorNode = new ChordNode(anchorNodeInfo);

                        ChordNode successor = anchorNode.findSuccessor(node.getNodeId());
                        node.setSuccessor(successor);
                        node.setPredecessor(successor.getPredecessor());
                        successor.setPredecessor(node);
                    } else {
                        // This is the first node in the network
                        node.setSuccessor(node);
                        node.setPredecessor(node);
                    }

                    // Update multicastService to use the new node
                    multicastService = new MulticastService(node, this::updateNodeList, port);
                    multicastService.start();

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
