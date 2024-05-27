package com.example.p2pnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.math.BigInteger;

public class NodeDetailsActivity extends AppCompatActivity {

    private TextView nodeId;
    private TextView predecessorId;
    private TextView successorId;
    private TextView fingerTable;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_details);

        nodeId = findViewById(R.id.nodeId);
        predecessorId = findViewById(R.id.predecessorId);
        successorId = findViewById(R.id.successorId);
        fingerTable = findViewById(R.id.fingerTable);
        backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        nodeId.setText("Node ID: " + intent.getStringExtra("nodeId"));
        predecessorId.setText("Predecessor ID: " + intent.getStringExtra("predecessorId"));
        successorId.setText("Successor ID: " + intent.getStringExtra("successorId"));
        fingerTable.setText("Finger Table:\n" + intent.getStringExtra("fingerTable"));

        backButton.setOnClickListener(v -> finish());
    }
}
