package com.example.p2pnetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
    private EditText ipAddressInput;
    private EditText portInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nodeStatus = findViewById(R.id.nodeStatus);
        joinNetworkButton = findViewById(R.id.joinNetworkButton);
        leaveNetworkButton = findViewById(R.id.leaveNetworkButton);
        viewDetailsButton = findViewById(R.id.viewDetailsButton);
        nodesRecyclerView = findViewById(R.id.nodesRecyclerView);
        ipAddressInput = findViewById(R.id.ipAddressInput);
        portInput = findViewById(R.id.portInput);
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

        // Set up item click listener for RecyclerView items
        nodesAdapter.setOnItemClickListener(nodeInfo -> {
            ipAddressInput.setText(nodeInfo.getIp().getHostAddress());
            portInput.setText(String.valueOf(nodeInfo.getPort()));
        });

        startMulticastService();
        LocalBroadcastManager.getInstance(this).registerReceiver(chatRequestReceiver, new IntentFilter("CHAT_REQUEST_RECEIVED"));

        joinNetworkButton.setOnClickListener(v -> new Thread(this::joinNetwork).start());
        leaveNetworkButton.setOnClickListener(v -> new Thread(this::leaveNetwork).start());
        viewDetailsButton.setOnClickListener(v -> viewNodeDetails());
        //startChatButton.setOnClickListener(v -> startChat());
    }

    private void onNodeItemClick(NodeInfo nodeInfo) {
        ipAddressInput.setText(nodeInfo.getIp().getHostAddress());
        portInput.setText(String.valueOf(nodeInfo.getPort()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(chatRequestReceiver);
    }

    private BroadcastReceiver chatRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String nodeId = intent.getStringExtra("nodeId");
            String ip = intent.getStringExtra("ip");
            int port = intent.getIntExtra("port", -1);

            // Open chat window
            Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
            chatIntent.putExtra("nodeId", nodeId);
            chatIntent.putExtra("ip", ip);
            chatIntent.putExtra("port", port);
            startActivity(chatIntent);
        }
    };

    private void startChat(String ip, int port) {
        if (node != null) {
            multicastService.sendChatRequest(node.getNodeId().toString(), ip, port);

            // Open chat window
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("nodeId", node.getNodeId().toString());
            intent.putExtra("ip", ip);
            intent.putExtra("port", port);
            startActivity(intent);
        }
    }

    // Call this method when the "Start Chat" button is clicked
    private void onStartChatButtonClick() {
        String ip = ipAddressInput.getText().toString();
        int port = Integer.parseInt(portInput.getText().toString());
        startChat(ip, port);
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

                    startTcpServer(); // Start the TCP server for chat

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

    private void fillInNodeDetails(NodeInfo nodeInfo) {
        ipAddressInput.setText(nodeInfo.getIp().getHostAddress());
        portInput.setText(String.valueOf(nodeInfo.getPort()));
    }

    private void startChat() {
        String ipAddress = ipAddressInput.getText().toString();
        String portString = portInput.getText().toString();
        int port = Integer.parseInt(portString);

        if (!ipAddress.isEmpty() && port > 0) {
            // Ensure this runs on a background thread
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    multicastService.sendMulticastMessage("CHAT_REQUEST," + ipAddress + "," + port);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("ipAddress", ipAddress);
                    intent.putExtra("port", port);
                    startActivity(intent);
                }
            }.execute();
        }
    }



    private void openChatActivity(String ip, int port) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("ip", ip);
        intent.putExtra("port", port);
        startActivity(intent);
    }
}
