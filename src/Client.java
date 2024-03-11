package src;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Client extends Thread {
    protected static String loginIp = "148.205.133.147";//"localhost";//"192.168.1.4"
    protected static int loginPort = 5050;
    protected static String tcpIp;//"192.168.10.10";
    protected static int tcpPort; // Port to send score update requests
    protected static String multicastIp;
    protected static int multicastPort;
    protected String usr = "";
    protected String pwd = "";
    protected String mode; //User or stress
    protected int score = 0;
    protected double loginRespTime;
    protected ArrayList<Double> hitRespTimes;
    protected int[] loginCounter = new int[2]; // [successes, failures]

    public Client(String mode) {
        this.mode = mode;
        hitRespTimes = new ArrayList<>();
    }

    public boolean tryLogin(String usr, String pwd) {
        boolean loginSuccess = false;
        long start, end; // Stress mode only

        try {
            Socket socket = new Socket(loginIp, loginPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            String[] loginData = {usr, pwd};
            out.writeObject(loginData);

            start = System.nanoTime();
            String[] conData = new String[4];
            conData = (String[])in.readObject();
            end = System.nanoTime();

            if(conData[0].equals("1")) {
                loginSuccess = true;
                multicastIp = conData[1];
                multicastPort = Integer.valueOf(conData[2]);
                tcpIp = conData[3];
                tcpPort = Integer.valueOf(conData[4]);
                score = Integer.valueOf(conData[5]);
                loginCounter[0]++;
                System.out.println("Multicast = " + multicastIp + ":" + multicastPort + ". TCP = " +
                                   tcpIp + ": " + tcpPort + ". Score =" + score + " received by " + usr);
            } else {
                loginCounter[1]++;
                System.out.println(usr + " could not login");
            }

            if(mode.equals("Stress"))
                loginRespTime = (end - start)/1e9;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return loginSuccess;
    }

    public String[] listenMulticast () {
        MulticastSocket mcastSocket = null;
        String msg = "", round = "-1", newMoleTile = "-1";
        String winner = "";
        long start, end; // Stress mode only

        try {
            InetAddress group = InetAddress.getByName(multicastIp); // destination multicast group
            mcastSocket = new MulticastSocket(multicastPort);
            mcastSocket.joinGroup(group);
            byte[] buffer = new byte[1000];

            start = System.nanoTime();
            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
            mcastSocket.receive(messageIn);
            end = System.nanoTime();

            byte[] mcastData = messageIn.getData();
            msg = new String(Arrays.copyOfRange(mcastData, 1, mcastData[0]+1));

            // Validates message source
            if(msg.equals("serverUpdate")) {
                round = String.valueOf(mcastData[mcastData[0] + 1]);
                newMoleTile = String.valueOf(mcastData[mcastData[0] + 2]);
                System.out.println(usr + " received mole at " + newMoleTile + " on round " + round + " from server");
            } else if(msg.equals("serverWinner")) {
                int from = msg.length()+2, to = from+mcastData[from-1];
                winner = new String(Arrays.copyOfRange(mcastData, from, to));
                System.out.println(usr + " received winner " + winner);
            }

            mcastSocket.leaveGroup(group);

            if(mode.equals("Stress"))
                hitRespTimes.add((end - start)/1e9);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if(mcastSocket != null) mcastSocket.close();
        }

        String[] out = {msg, round, newMoleTile, winner};
        return out;
    }

    public boolean reqScoreUpdate (int round) {
        boolean reqApproved = false;

        try {
            Socket socket = new Socket(tcpIp, tcpPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            String[] reqData = {String.valueOf(round), usr};
            out.writeObject(reqData);

            String[] hit;
            hit = (String[])in.readObject();

            if(hit[0].equals("hit")) {
                reqApproved = true;
                score = Integer.parseInt(hit[1]);
                System.out.println(usr + " received a hit. New score is " + score);
            } else if(hit[0].equals("win")) {
                reqApproved = true;
                score = Integer.parseInt(hit[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return reqApproved;
    }

    @Override
    public void run() {}

    public String getUsr() { return usr; }

    public int getScore() { return score; }

    public double getLoginRespTime() { return loginRespTime; }

    public void setUsr(String usr) { this.usr = usr; }

    public void setPwd(String pwd) { this.pwd = pwd; }

    public void setScore(int score) { this.score = score; }
}
