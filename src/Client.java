package src;

import java.net.*;
import java.io.*;

public class Client extends Thread {
    protected static String loginIp = "224.0.0.1";
    protected static int loginPort = 5050;
    protected static String multicastIp;
    protected static int multicastPort;
    protected byte[] grid; //0: no mole, 1: mole
    protected int mode = 0; //0: user mode, 1: stress mode
    protected int score = 0;

    public boolean tryLogin() {
        boolean loginSuccess = false;

        try {
            Socket socket = new Socket(loginIp, loginPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // ObjectStream
            String[] connData = new String[0];

            connData = (String[])in.readObject();
            System.out.println("IP: port received by Client: " + connData[0] + ":" + connData[1]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return loginSuccess;
    }

    public void listenMulticast () {
        MulticastSocket mcastSocket = null;

        try {
            InetAddress group = InetAddress.getByName(multicastIp); // destination multicast group
            mcastSocket = new MulticastSocket(multicastPort);
            mcastSocket.joinGroup(group);
            byte[] buffer = new byte[1000];

            System.out.println("Waiting for messages");
            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
            mcastSocket.receive(messageIn);
            System.out.println("Message: " + new String(messageIn.getData()).trim() + " from: " + messageIn.getAddress());

            mcastSocket.leaveGroup(group);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if(mcastSocket != null) mcastSocket.close();
        }
    }

    public void updateGrid () {
        try {
            InetAddress group = InetAddress.getByName(multicastIp); // destination multicast group 228.5.6.7
            MulticastSocket socket = new MulticastSocket(multicastPort);
            socket.joinGroup(group);
            byte[] buf = new byte[1000];

            System.out.println("Waiting for messages");
            DatagramPacket messageIn = new DatagramPacket(buf, buf.length);
            socket.receive(messageIn);

            //TODO: update
            //System.out.println(new String(messageIn.getData()));
            //System.out.println("Message: " + new String(messageIn.getData()).trim() + " from: " + messageIn.getAddress());
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // TODO: método click

    // TODO: crear ui

    public void reqScoreUpdate () {
        try {
            Socket socket = new Socket("localhost", 1);
            //s = new Socket("127.0.0.1", serverPort);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // ObjectStream
            String[] connData = (String[])in.readObject();
            System.out.println("IP: port received by Client: " + connData[0] + ":" + connData[1]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Socket socket = null;

        try {
            tryLogin();

            while(true) {
                // Listens for user click

                // Listens for multicast


                // Requests score change to server after hitting mole

            }
        } finally {
            if (socket != null) try {
                socket.close();
            } catch (IOException e) {
                System.out.println("close:" + e.getMessage());
            }
        }

    }
}
