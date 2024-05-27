package com.example.p2pnetwork;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NodeDetailsActivity extends AppCompatActivity {

    private TextView nodeIdText;
    private TextView predecessorIdText;
    private TextView successorIdText;
    private TextView fingerTableText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_details);

        nodeIdText = findViewById(R.id.nodeId);
        predecessorIdText = findViewById(R.id.predecessorId);
        successorIdText = findViewById(R.id.successorId);
        fingerTableText = findViewById(R.id.fingerTable);

        String nodeId = getIntent().getStringExtra("nodeId");
        String predecessorId = getIntent().getStringExtra("predecessorId");
        String successorId = getIntent().getStringExtra("successorId");
        String fingerTable = getIntent().getStringExtra("fingerTable");

        nodeIdText.setText(nodeId);
        predecessorIdText.setText(predecessorId != null ? predecessorId : "None");
        successorIdText.setText(successorId != null ? successorId : "None");
        fingerTableText.setText(fingerTable != null ? fingerTable : "None");
    }
}
