// MainActivity.java
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
    private Button viewDetailsButton;
    private RecyclerView nodesRecyclerView;
    private NodesAdapter nodesAdapter;
    private List<NodeInfo> nodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nodeStatus = findViewById(R.id.nodeStatus);
        Button joinNetworkButton = findViewById(R.id.joinNetworkButton);
        viewDetailsButton = findViewById(R.id.viewDetailsButton);
        nodesRecyclerView = findViewById(R.id.nodesRecyclerView);

        nodeList = new ArrayList<>();
        nodesAdapter = new NodesAdapter(nodeList);
        nodesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nodesRecyclerView.setAdapter(nodesAdapter);

        joinNetworkButton.setOnClickListener(v -> new Thread(() -> {
            try {
                InetAddress ip = NetworkUtils.getIPAddress();
                int port = 5000; // Example port, ensure to set this appropriately

                if (ip != null) {
                    node = new ChordNode(ip);

                    multicastService = new MulticastService(node, this::updateNodeList);
                    multicastService.start();

                    stabilizationService = new StabilizationService(node, multicastService);
                    stabilizationService.start();

                    runOnUiThread(() -> {
                        nodeStatus.setText("Node ID: " + node.getNodeId());
                        viewDetailsButton.setEnabled(true);
                    });

                    multicastService.sendMulticastMessage(node.getNodeId() + "," + ip.getHostAddress() + "," + port);
                } else {
                    runOnUiThread(() -> nodeStatus.setText("Failed to get IP address"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start());

        viewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NodeDetailsActivity.class);
            intent.putExtra("nodeId", node.getNodeId().toString());
            intent.putExtra("predecessorId", node.getPredecessor() != null ? node.getPredecessor().getNodeId().toString() : "None");
            intent.putExtra("successorId", node.getSuccessor() != null ? node.getSuccessor().getNodeId().toString() : "None");
            intent.putExtra("fingerTable", node.getFingerTableAsString());
            startActivity(intent);
        });
    }

    private void updateNodeList(List<NodeInfo> newNodeList) {
        runOnUiThread(() -> nodesAdapter.updateNodes(newNodeList));
    }
}
