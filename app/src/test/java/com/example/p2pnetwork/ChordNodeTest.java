package com.example.p2pnetwork;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.p2pnetwork.models.NodeInfo;
import com.example.p2pnetwork.network.ChordNode;

import javax.xml.transform.Source;

public class ChordNodeTest {

    private InetAddress ip;
    private int dynamicPort;
    private BigInteger nodeId;


    //initializes some variables
    @Before
    public void setUp() throws Exception {
        ip = InetAddress.getByName("192.168.0.105");
        dynamicPort = 8080;
        nodeId = new BigInteger("1234567890abcdef1234567890abcdef12345678", 16); // Example node ID
    }


    //tests if the ChordNode is created correctly from the NodeInfo-constructor
    @Test
    public void testChordNode_NodeInfo() {
        System.out.println("\ntestChordNode_NodeInfo start");


        NodeInfo nodeInfo = new NodeInfo(nodeId, ip, dynamicPort);
        ChordNode chordNode = new ChordNode(nodeInfo);
        System.out.println("0");

        assertNotNull(chordNode);
        System.out.println("1");
        assertEquals(ip, chordNode.getIp());
        System.out.println("2");
        assertEquals(dynamicPort, chordNode.getDynamicPort());
        System.out.println("3");
        assertEquals(nodeId, chordNode.getNodeId());
        System.out.println("4");
        assertNull(chordNode.getPredecessor());
        System.out.println("5");

        System.out.println("testChordNode_NodeInfo passed");
    }

    //creates a node from a port and ip, checks if its correctly assigned, and that a hashed id is created
    //also checks if the predecessor is set
    @Test
    public void testChordNode_IpAndPort() {
        System.out.println("\ntestChordNode_IpAndPort start");



        ChordNode chordNode = new ChordNode(ip, dynamicPort);
        System.out.println("0");


        assertNotNull(chordNode);
        System.out.println("1");
        assertEquals(ip, chordNode.getIp());
        System.out.println("2");
        assertEquals(dynamicPort, chordNode.getDynamicPort());
        System.out.println("3");
        assertNotNull(chordNode.getNodeId());
        System.out.println("4");
        assertNull(chordNode.getPredecessor());
        System.out.println("5");

        System.out.println("testChordNode_IpAndPort passed");
    }

    //creates a node from a port, ip and node id
    //checks the node is not null
    //checks the ip is equal to the given ip
    //checks the port is equal to the given port
    //checks the id is equal to the given id
    //checks if a predecessor is set
    @Test
    public void testChordNode_IpNodeIdPort() {
        System.out.println("\ntestChordNode_IpNodeIdPort start");


        ChordNode chordNode = new ChordNode(ip, nodeId, dynamicPort);
        System.out.println("0");

        assertNotNull(chordNode);
        System.out.println("1");
        assertEquals(ip, chordNode.getIp());
        System.out.println("2");
        assertEquals(dynamicPort, chordNode.getDynamicPort());
        System.out.println("3");
        assertEquals(nodeId, chordNode.getNodeId());
        System.out.println("4");
        assertNull(chordNode.getPredecessor());
        System.out.println("5");

        System.out.println("testChordNode_IpNodeIdPort passed");
    }
}
