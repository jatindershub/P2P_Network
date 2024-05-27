package com.example.p2pnetwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NodeDetailsActivity extends AppCompatActivity {

    private TextView nodeIdTextView;
    private TextView predecessorIdTextView;
    private TextView successorIdTextView;
    private LinearLayout fingerTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_details);

        nodeIdTextView = findViewById(R.id.nodeIdTextView);
        predecessorIdTextView = findViewById(R.id.predecessorIdTextView);
        successorIdTextView = findViewById(R.id.successorIdTextView);
        fingerTableLayout = findViewById(R.id.fingerTableLayout);

        Intent intent = getIntent();
        String nodeId = intent.getStringExtra("nodeId");
        String predecessorId = intent.getStringExtra("predecessorId");
        String successorId = intent.getStringExtra("successorId");
        String[] fingerTable = intent.getStringArrayExtra("fingerTable");

        nodeIdTextView.setText("Node ID: " + nodeId);
        predecessorIdTextView.setText("Predecessor ID: " + predecessorId);
        successorIdTextView.setText("Successor ID: " + successorId);

        for (String entry : fingerTable) {
            TextView fingerEntryView = new TextView(this);
            fingerEntryView.setText(entry);
            fingerTableLayout.addView(fingerEntryView);
        }
    }
}

