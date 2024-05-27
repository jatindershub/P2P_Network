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
    private MulticastService multicastService;
    private StabilizationService stabilizationService;
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

                multicastService = new MulticastService(node);
                multicastService.start();

                stabilizationService = new StabilizationService(node, multicastService);
                stabilizationService.start();

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
