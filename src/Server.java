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
    protected static final int WAIT_BETWEEN_ROUNDS = 5000;
    protected static final int MOLE_TIMER = 2000;
    protected Map<String, Player> usrPlayerMap; // ip, port, usr, pwd, points
    protected boolean[] claimedRounds; // Keeps track of which rounds have been won by a player
    protected boolean gameOver = false;
    protected String winner = "";
    //protected byte round = 1;
    //protected byte moleTile = -1;

    public Server() {
        loginServer = new LoginServer(this);
        multicastServer = new MulticastServer(this);
        loginServer.start();
        multicastServer.start();
        usrPlayerMap = new HashMap<>();
        claimedRounds = new boolean[1000];
        Arrays.fill(claimedRounds, false);
    }

    public void notifyWinner() {
        try {
            InetAddress group = InetAddress.getByName(Server.multicastIp); // destination multicast group
            MulticastSocket socket = new MulticastSocket(Server.multicastPort);
            socket.joinGroup(group);
            socket.setTimeToLive(50);

            byte[] msg = "serverWinner".getBytes(); // Ensures the multicast's source is the server

            byte[] buffer = multicastServer.createOutMsg(msg, winner.getBytes());
            DatagramPacket messageOut = new DatagramPacket(buffer, buffer.length, group, Server.multicastPort);
            socket.send(messageOut);
            System.out.println("========= GAME END =========");
            System.out.println("Sent winner " + winner + " via multicast");
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Excp: " + e.getMessage());
        }
    }

    public void endGame(String usr) {
        winner = usr;
        notifyWinner();
        multicastServer.reset();
        claimedRounds = new boolean[1000];
        Arrays.fill(claimedRounds, false);

        for(Player player: usrPlayerMap.values())
            player.resetScore();

        // Waits before starting another round
        try{ Thread.sleep(WAIT_BETWEEN_ROUNDS); } catch (InterruptedException e){ e.printStackTrace(); }

        gameOver = false;
        winner = "";
    }

    @Override
    public void run() {
        try {
            ServerSocket listenSocket = new ServerSocket(tcpPort);

            while(true) {
                Socket clientSocket = listenSocket.accept(); // Waits for a connection
                TCPConnection con = new TCPConnection(clientSocket);
                con.start();

                // Receives request from client to update score
                String[] data = (String[])con.in.readObject();
                int reqRound = Integer.valueOf(data[0]);
                String usr = data[1];
                System.out.println("Got " + data[0] + " from " + usr);
                String[] msgOut = {""};

                if(reqRound == multicastServer.round && !claimedRounds[multicastServer.round]) {
                    claimedRounds[multicastServer.round] = true;
                    usrPlayerMap.get(usr).updateScore();
                    int score = usrPlayerMap.get(usr).getScore();

                    if (score >= POINTS_TO_WIN) {
                        msgOut[0] = "win";
                        gameOver = true;
                        System.out.println(usr + " wins");
                    } else {
                        msgOut[0] = "hit";
                        System.out.println(usr + " got a hit. New score is " + score);
                    }
                } else {
                    msgOut[0] = "miss";
                    System.out.println(usr + " got a miss");
                }

                con.out.writeObject(msgOut);

                if(gameOver)
                    endGame(usr);
            }
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getWinner() {
        return winner;
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
    protected Server server;
    protected byte round = 0;
    protected byte moleTile = -1;

    public MulticastServer(Server server) { this.server = server; }

    public void sendMulticast() {
        try {
            while(true) {
                // Selects a random new mole position which is different from the last
                Random rand = new Random();
                int[] range = IntStream.rangeClosed(0, 8).toArray();
                int[] filteredRange = Arrays.stream(range).filter(value -> value != moleTile).toArray();
                moleTile = (byte)filteredRange[rand.nextInt(8)];
                //moleTile = (byte)rand.nextInt(9);
                round++;

                InetAddress group = InetAddress.getByName(Server.multicastIp); // destination multicast group
                MulticastSocket socket = new MulticastSocket(Server.multicastPort);
                socket.joinGroup(group);
                socket.setTimeToLive(50);

                byte[] msg; // Ensures the multicast's source is the server

                if(!server.isGameOver()) {
                    msg = "serverUpdate".getBytes();

                    byte[] buffer = createOutMsg(msg, round, moleTile);
                    DatagramPacket messageOut = new DatagramPacket(buffer, buffer.length, group, Server.multicastPort);
                    socket.send(messageOut);
                    System.out.println("========= ROUND " + round + " =========");
                    System.out.println("Sent " + "[" + round + ", " + moleTile + "]" + " via multicast");

                    // Waits before sending another mole
                    try { Thread.sleep(Server.MOLE_TIMER); } catch (InterruptedException e) { e.printStackTrace(); }
                } else {
                    // Waits between rounds
                    try { Thread.sleep(Server.WAIT_BETWEEN_ROUNDS+1000); } catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Excp: " + e.getMessage());
        }
    }

    public static byte[] createOutMsg(byte[] msg, byte[] winner) {
        // If there's a winner, send his usr
        byte[] buffer = new byte[msg.length + winner.length + 2];;
        buffer[0] = (byte)msg.length;

        for(int i = 0; i < msg.length; i++)
            buffer[i+1] = msg[i];

        buffer[msg.length + 1] = (byte)winner.length;

        for(int i = 0; i < winner.length; i++)
            buffer[msg.length + 2 + i] = winner[i];

        return buffer;
    }

    public static byte[] createOutMsg(byte[] msg, byte round, byte moleTile) {
        // If there's no winner yet, update moles
        byte[] buffer = new byte[msg.length + 3];;
        buffer[0] = (byte)msg.length;

        for(int i = 0; i < msg.length; i++)
            buffer[i + 1] = msg[i];

        buffer[buffer.length - 2] = round;
        buffer[buffer.length - 1] = moleTile;

        return buffer;
    }

    public void reset() {
        round = 0;
        moleTile = -1;
    }

    @Override
    public void run() {
        sendMulticast();
    }
}
