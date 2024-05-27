package com.example.p2pnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

        nodeList = new ArrayList<>();
        nodesAdapter = new NodesAdapter(nodeList);
        nodesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nodesRecyclerView.setAdapter(nodesAdapter);

        startMulticastService();

        joinNetworkButton.setOnClickListener(v -> joinNetwork());
        leaveNetworkButton.setOnClickListener(v -> leaveNetwork());
        viewDetailsButton.setOnClickListener(v -> viewNodeDetails());
    }

    private void startMulticastService() {
        InetAddress ip = NetworkUtils.getIPAddress();
        int port = NetworkUtils.getAvailablePort();

        if (ip != null && port != -1) {
            multicastService = new MulticastService(this::updateNodeList, port);
            multicastService.start();
        } else {
            runOnUiThread(() -> nodeStatus.setText("Failed to get IP address or port"));
        }
    }

    private void joinNetwork() {
        new Thread(() -> {
            try {
                InetAddress ip = NetworkUtils.getIPAddress();
                int port = NetworkUtils.getAvailablePort();

                if (ip != null && port != -1) {
                    node = new ChordNode(ip, port);

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
        new Thread(() -> {
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
        }).start();
    }

    private void viewNodeDetails() {
        Intent intent = new Intent(MainActivity.this, NodeDetailsActivity.class);
        intent.putExtra("nodeId", node.getNodeId().toString());
        intent.putExtra("predecessorId", node.getPredecessor() != null ? node.getPredecessor().getNodeId().toString() : "None");
        intent.putExtra("successorId", node.getSuccessor() != null ? node.getSuccessor().getNodeId().toString() : "None");
        intent.putExtra("fingerTable", node.getFingerTableAsString());
        startActivity(intent);
    }

    private void updateNodeList(List<NodeInfo> newNodeList) {
        runOnUiThread(() -> nodesAdapter.updateNodes(newNodeList));
    }
}
