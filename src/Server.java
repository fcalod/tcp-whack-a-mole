package src;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

/*
* Threads:
*  Server - listens for clients who hit a mole
*  Login - listens for clients who want to join the game
*  Multicast - continually sends out game updates
* */
public class Server extends Thread {
    protected LoginServer loginServer;
    protected MulticastServer multicastServer;
    protected static final String loginIp = "localhost";//"192.168.10.10";
    protected static final int loginPort = 5050;
    protected static final String tcpIp = "localhost";//"192.168.10.10";
    protected static final int tcpPort = 4040;
    protected static final String multicastIp = "224.0.0.251";
    protected static final int multicastPort = 5353;
    protected static final int POINTS_TO_WIN = 5;
    protected Map<String, Player> usrPlayerMap; // ip, port, usr, pwd, points
    //protected byte round = 1;
    //protected byte moleTile = -1;

    public Server() {
        loginServer = new LoginServer(this);
        multicastServer = new MulticastServer();
        loginServer.start();
        multicastServer.start();
        usrPlayerMap = new HashMap<>();
    }

    public void endGame() {
        //TODO: resettear todos los puntajes y mandar por multicast el nombre del ganador
        multicastServer.reset();
    }

    @Override
    public void run() {
        try {
            ServerSocket listenSocket = new ServerSocket(tcpPort);

            while(true) {
                Socket clientSocket = listenSocket.accept(); // Waits for a connection
                TCPConnection con = new TCPConnection(clientSocket);
                con.start();

                // Receives request from
                String[] data = (String[])con.in.readObject();
                String usr = data[0];
                int score = usrPlayerMap.get(usr).getScore();
                String[] msgOut = {""};

                if(score >= POINTS_TO_WIN) {
                    msgOut[0] = "win";
                    System.out.println(usr + " wins");
                } else {
                    msgOut[0] = "hit";
                    con.out.writeObject(msgOut);
                    System.out.println(usr + " got a hit");
                }
            }
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
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
}

class LoginServer extends Thread {
    Server server;

    public LoginServer(Server server) {
        this.server = server;
    }

    public void verifyLogin(TCPConnection con) {
        try {
            boolean loginSuccess = false;
            String[] data = (String[])con.in.readObject(); // Waits for client to connect
            String usr = data[0];
            String pwd = data[1];

            if(server.usrPlayerMap.get(usr) != null) {
                // Player exists; checks password
                String correctPwd = server.usrPlayerMap.get(usr).getPwd();

                if(pwd.equals(correctPwd)) {
                    loginSuccess = true;
                } else {
                    String[] msgOut = {"-1", "Incorrect pwd", ""};
                    con.out.writeObject(msgOut);
                    System.out.println("Received incorrect pwd for: " + usr);
                }
            } else {
                // Player doesn't exist; creates new player
                Player newPlayer  = new Player(usr, pwd);
                server.usrPlayerMap.put(usr, newPlayer);
                loginSuccess = true;
            }

            if(loginSuccess) {
                // Sends connection details to client
                String multicastIp = Server.multicastIp;
                String multicastPort = String.valueOf(Server.multicastPort);
                String tcpIp = Server.tcpIp;
                String tcpPort = String.valueOf(Server.tcpPort);
                String score = String.valueOf(server.usrPlayerMap.get(usr).getScore());
                String[] msgOut = {"1", multicastIp, multicastPort, tcpIp, tcpPort, score};
                con.out.writeObject(msgOut);
                System.out.println("Multicast = " + multicastIp + ":" + multicastPort + ", TCP = " +
                                   tcpIp + ":" + tcpPort + " sent by Server to " + usr);
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
            ServerSocket listenSocket = new ServerSocket(Server.loginPort);

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

class MulticastServer extends Thread {
    protected byte round = 1;
    protected byte moleTile = -1;

    public MulticastServer() {}

    public void sendMulticast() {
        try {
            while(true) {
                // Selects a random new mole position which is different from the last
                Random rand = new Random();
                /*int[] range = IntStream.rangeClosed(0, 8).toArray();
                int[] filteredRange = Arrays.stream(range).filter(value -> value != moleTile).toArray();
                moleTile = (byte)filteredRange[rand.nextInt(8)];*/
                moleTile = (byte)rand.nextInt(9);

                InetAddress group = InetAddress.getByName(Server.multicastIp); // destination multicast group
                MulticastSocket socket = new MulticastSocket(Server.multicastPort);
                socket.joinGroup(group);
                socket.setTimeToLive(50);

                byte[] buffer = {round, moleTile};
                DatagramPacket messageOut = new DatagramPacket(buffer, buffer.length, group, Server.multicastPort);
                socket.send(messageOut);
                System.out.println("========= Round " + round + " =========");
                System.out.println("Sent " + Arrays.toString(buffer) + " via multicast");
                round++;

                // Waits before sending another mole
                try{
                    Thread.sleep(5000);
                } catch (InterruptedException e){
                    System.exit(0);
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Excp: " + e.getMessage());
        }
    }

    public void reset() {
        round = 1;
        moleTile = -1;
    }

    @Override
    public void run() {
        sendMulticast();
    }
}
