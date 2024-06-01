package com.example.p2pnetwork;

import org.junit.Test;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.net.ServerSocket;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.p2pnetwork.network.NetworkUtils;

public class NetworkUtilsTest {

    //checks if a local free ip address exists
    @Test
    public void testGetIPAddress() {
        System.out.println("testGetIPAddress start");


        InetAddress result = NetworkUtils.getIPAddress();
        assertTrue(result != null);
        if (result!=null){
            assertNotNull(result);
            System.out.println("testGetIPAddress success: IP address found: " + result.getHostAddress());
        }else {
            System.out.println("testGetIPAddress failed");
        }

    }

    //checks for an available port
    //binds the available port to a socket
    @Test
    public void testGetAvailablePort() {
        System.out.println("testGetAvailablePort start");


        int port = NetworkUtils.getAvailablePort();
        assertTrue(port > 0 && port <= 65535);

        System.out.println("Available port found: " + port);

        //try in order to shut down the socket when test is over
        try (ServerSocket socket = new ServerSocket(port)) {
            assertTrue(socket.getLocalPort() == port);
            System.out.println("testGetAvailablePort success: bound to port: " + port);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("testGetAvailablePort failed");
        }
    }

}
