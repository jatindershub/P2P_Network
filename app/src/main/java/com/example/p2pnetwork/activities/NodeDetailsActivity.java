package com.example.p2pnetwork.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p2pnetwork.R;

public class NodeDetailsActivity extends AppCompatActivity {

    private TextView nodeIdTextView;
    private TextView predecessorIdTextView;
    private TextView successorIdTextView;
    private LinearLayout fingerTableLayout;
    private ScrollView fingerTableScrollView;
    private Button toggleFingerTableButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_details);

        nodeIdTextView = findViewById(R.id.nodeIdTextView);
        predecessorIdTextView = findViewById(R.id.predecessorIdTextView);
        successorIdTextView = findViewById(R.id.successorIdTextView);
        fingerTableLayout = findViewById(R.id.fingerTableLayout);
        fingerTableScrollView = findViewById(R.id.fingerTableScrollView);
        toggleFingerTableButton = findViewById(R.id.toggleFingerTableButton);

        Intent intent = getIntent();
        String nodeId = intent.getStringExtra("nodeId");
        String predecessorId = intent.getStringExtra("predecessorId");
        String successorId = intent.getStringExtra("successorId");
        String[] fingerTable = intent.getStringArrayExtra("fingerTable");

        nodeIdTextView.setText(nodeId);
        predecessorIdTextView.setText(predecessorId);
        successorIdTextView.setText(successorId);

        for (String entry : fingerTable) {
            TextView fingerEntryView = new TextView(this);
            fingerEntryView.setText(entry);
            fingerTableLayout.addView(fingerEntryView);
        }

        toggleFingerTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fingerTableScrollView.getVisibility() == View.GONE) {
                    fingerTableScrollView.setVisibility(View.VISIBLE);
                    toggleFingerTableButton.setText("Hide Finger Table");
                } else {
                    fingerTableScrollView.setVisibility(View.GONE);
                    toggleFingerTableButton.setText("Show Finger Table");
                }
            }
        });
    }
}
