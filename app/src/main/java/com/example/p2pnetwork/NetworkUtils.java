package com.example.p2pnetwork;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public class NetworkUtils {

    public static InetAddress getIPAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress address : Collections.list(ni.getInetAddresses())) {
                    if (!address.isLoopbackAddress() && address.isSiteLocalAddress()) {
                        return address;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
