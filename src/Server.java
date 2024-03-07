package src;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server extends Thread {
    protected static String multicastIp = "224.0.0.251";
    protected static int multicastPort = 5353;
    protected static int MAX_CLIENTS = 10000;
    protected static int POINTS_TO_WIN = 5;
    protected byte[] grid; //0: no mole, 1: mole
    //protected TCPConnection[] clientCons;
    protected Player[] playerData; // ip, port, usr, pwd, points
    protected Map<String, Player> usrPlayerMap;

    public Server() {
        //this.clientCons = new TCPConnection[MAX_CLIENTS];
        grid = new byte[9];
        this.playerData = new Player[MAX_CLIENTS];
        usrPlayerMap = new HashMap<>();
    }

    public void sendMulticast() {
        try {
            while(true) {
                InetAddress group = InetAddress.getByName(multicastIp); // destination multicast group
                MulticastSocket socket = new MulticastSocket(multicastPort);
                socket.joinGroup(group);
                socket.setTimeToLive(10);

                DatagramPacket messageOut = new DatagramPacket(grid, grid.length, group, multicastPort);
                socket.send(messageOut);
                System.out.println("Sent " + Arrays.toString(grid) + " via multicast");
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Excp: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        LoginServer loginServer = new LoginServer();
        loginServer.start();

        try {
            ServerSocket listenSocket = new ServerSocket(multicastPort);

            while(true) {
                // Sends moles via multicast
                server.sendMulticast();

                // Listens for client response


                // Updates game state

            }
        } catch (IOException e) {
            System.out.println("Listen :" + e.getMessage());
        }
    }
}

class TCPConnection extends Thread {
    public ObjectInputStream in;

    public ObjectOutputStream out;
    protected Socket clientSocket;

    public TCPConnection() {}

    public TCPConnection(Socket clientSocket) {
        try {
            clientSocket = clientSocket;
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // Sends connection details to client
            String ip = "224.0.0.251";
            String port = "5353";
            String[] msgOut = {ip, port};
            out.writeObject(msgOut);
            System.out.println("IP: port sent by Server: " + ip + ":" + port);
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}

class LoginServer extends Server {
    protected static String loginIp = "224.0.0.1";
    protected static int loginPort = 5050;
    protected TCPConnection[] clientCons;
    protected Player[] playerData; // ip, port, usr, pwd, points

    public LoginServer() {
    }

    public void verifyLogin(TCPConnection con) {
        try {
            boolean loginSuccess = false;
            String[] data = (String[])con.in.readObject();
            String usr = data[0];
            String pwd = data[1];

            if(usrPlayerMap.get(usr) != null) {
                // Player exists; checks password
                String correctPwd = usrPlayerMap.get(usr).getPwd();

                if(pwd.equals(correctPwd)) {
                    loginSuccess = true;
                } else {
                    String[] msgOut = {"Incorrect pwd"};
                    con.out.writeObject(msgOut);
                    System.out.println("Received incorrect pwd for: " + usr);
                }
            } else {
                // Player doesn't exist; creates new player
                Player newPlayer  = new Player(usr, pwd);
                loginSuccess = true;
            }

            if(loginSuccess) {
                // Sends connection details to client
                String ip = "224.0.0.251";
                String port = "5353";
                String[] msgOut = {ip, port};
                con.out.writeObject(msgOut);
                System.out.println("IP: port sent by Server: " + ip + ":" + port);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket listenSocket = new ServerSocket(loginPort);

            while(true) {
                Socket clientSocket = listenSocket.accept(); // Waits for a connection
                TCPConnection con = new TCPConnection(clientSocket);
                con.start();
                verifyLogin(con);
            }
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

}
