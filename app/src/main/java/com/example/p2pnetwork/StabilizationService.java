package com.example.p2pnetwork;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.math.BigInteger;

public class StabilizationService extends Thread {
    private static final String TAG = "StabilizationService";

    private ChordNode localNode;
    private MulticastService multicastService;
    private Handler handler;
    private volatile boolean running = true;

    public StabilizationService(ChordNode localNode, MulticastService multicastService) {
        this.localNode = localNode;
        this.multicastService = multicastService;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
        handler.post(stabilizeRunnable);
    }

    private final Runnable stabilizeRunnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                new Thread(() -> stabilize()).start();
                handler.postDelayed(this, 5000); // Repeat every 5 seconds
            }
        }
    };

    private void stabilize() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Stabilization interval

                    // Send out a multicast message to inform other nodes of this node's existence
                    multicastService.sendMulticastMessage("JOIN," + localNode.getNodeId() + "," + localNode.getIp().getHostAddress() + "," + localNode.getDynamicPort());

                    // Check and correct the successor and predecessor
                    ChordNode successor = localNode.getSuccessor();
                    if (successor != null) {
                        ChordNode successorPredecessor = successor.getPredecessor();
                        if (successorPredecessor != null && isInInterval(successorPredecessor.getNodeId(), localNode.getNodeId(), successor.getNodeId())) {
                            localNode.setSuccessor(successorPredecessor);
                        }
                    } else {
                        localNode.setSuccessor(localNode);
                    }

                    ChordNode predecessor = localNode.getPredecessor();
                    if (predecessor != null) {
                        ChordNode predecessorSuccessor = predecessor.getSuccessor();
                        if (predecessorSuccessor != null && isInInterval(predecessorSuccessor.getNodeId(), predecessor.getNodeId(), localNode.getNodeId())) {
                            localNode.setPredecessor(predecessorSuccessor);
                        }
                    } else {
                        localNode.setPredecessor(localNode);
                    }

                    // Update finger table periodically
                    for (int i = 0; i < ChordNode.M; i++) {
                        BigInteger start = localNode.getNodeId().add(BigInteger.valueOf(2).pow(i)).mod(BigInteger.valueOf(2).pow(ChordNode.M));
                        ChordNode successorNode = localNode.findSuccessor(start);
                        if (successorNode != null) {
                            localNode.updateFingerTable(successorNode);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




    private boolean isInInterval(BigInteger id, BigInteger start, BigInteger end) {
        if (start.compareTo(end) < 0) {
            return id.compareTo(start) > 0 && id.compareTo(end) <= 0;
        } else {
            return id.compareTo(start) > 0 || id.compareTo(end) <= 0;
        }
    }

    public void stopService() {
        running = false;
        handler.removeCallbacks(stabilizeRunnable);
    }
}
