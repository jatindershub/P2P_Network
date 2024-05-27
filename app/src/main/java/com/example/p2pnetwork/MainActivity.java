package com.example.p2pnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private ChordNode node;
    private ChordNode existingNode; // Add a reference to an existing node for initialization
    private MulticastService multicastService;
    private TextView nodeStatus;
    private Button viewDetailsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nodeStatus = findViewById(R.id.nodeStatus);
        Button joinNetworkButton = findViewById(R.id.joinNetworkButton);
        viewDetailsButton = findViewById(R.id.viewDetailsButton);

        joinNetworkButton.setOnClickListener(v -> new Thread(() -> {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                node = new ChordNode(ip);

                // Simulate an existing node for initializing the finger table
                existingNode = new ChordNode(InetAddress.getByName("192.168.1.2")); // Replace with actual node address
                existingNode.setSuccessor(existingNode); // Set the existing node's successor to itself for simplicity
                existingNode.setPredecessor(existingNode); // Set the existing node's predecessor to itself for simplicity
                node.initializeFingerTable(existingNode);

                multicastService = new MulticastService();
                multicastService.start();

                runOnUiThread(() -> {
                    nodeStatus.setText("Node ID: " + node.getNodeId());
                    viewDetailsButton.setEnabled(true);
                });
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
}
