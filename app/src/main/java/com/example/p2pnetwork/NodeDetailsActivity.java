package com.example.p2pnetwork;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NodeDetailsActivity extends AppCompatActivity {

    private TextView nodeIdText;
    private TextView predecessorIdText;
    private TextView successorIdText;
    private LinearLayout fingerTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_details);

        nodeIdText = findViewById(R.id.nodeIdText);
        predecessorIdText = findViewById(R.id.predecessorIdText);
        successorIdText = findViewById(R.id.successorIdText);
        fingerTableLayout = findViewById(R.id.fingerTableLayout);

        String nodeId = getIntent().getStringExtra("nodeId");
        String predecessorId = getIntent().getStringExtra("predecessorId");
        String successorId = getIntent().getStringExtra("successorId");
        String fingerTable = getIntent().getStringExtra("fingerTable");

        nodeIdText.setText("Node ID: " + nodeId);
        predecessorIdText.setText("Predecessor ID: " + predecessorId);
        successorIdText.setText("Successor ID: " + successorId);

        if (fingerTable != null && !fingerTable.isEmpty()) {
            String[] fingerTableEntries = fingerTable.split(",");
            for (String entry : fingerTableEntries) {
                TextView textView = new TextView(this);
                textView.setText(entry);
                textView.setTextSize(16);
                textView.setPadding(0, 4, 0, 4);
                fingerTableLayout.addView(textView);
            }
        }
    }
}
