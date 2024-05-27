package com.example.p2pnetwork;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private ChordNode node;
    private MulticastService multicastService;
    private TextView nodeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nodeStatus = findViewById(R.id.nodeStatus);
        Button joinNetworkButton = findViewById(R.id.joinNetworkButton);

        joinNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InetAddress ip = InetAddress.getLocalHost();
                            node = new ChordNode(ip);
                            multicastService = new MulticastService();
                            multicastService.start();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nodeStatus.setText("Node ID: " + node.getNodeId());
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}
