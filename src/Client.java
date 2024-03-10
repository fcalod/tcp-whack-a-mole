package src;

import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.util.Arrays;

public class Client extends Thread {
    protected static String loginIp = "localhost";//"192.168.10.10";
    protected static int loginPort = 5050;
    protected static String tcpIp;//"192.168.10.10";
    protected static int tcpPort; // Port to send score update requests
    protected static String multicastIp;
    protected static int multicastPort;
    protected String usr = "";
    protected String pwd = "";
    protected String mode; //User or stress
    protected int score = 0;
    protected int clickedTile = -1;

    public Client(String mode) {
        this.mode = mode;
    }

    public boolean tryLogin(String usr, String pwd) {
        boolean loginSuccess = false;

        try {
            Socket socket = new Socket(loginIp, loginPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            String[] loginData = {usr, pwd};
            out.writeObject(loginData);

            String[] conData = new String[4];
            conData = (String[])in.readObject();

            if(conData[0].equals("1")) {
                loginSuccess = true;
                multicastIp = conData[1];
                multicastPort = Integer.valueOf(conData[2]);
                tcpIp = conData[3];
                tcpPort = Integer.valueOf(conData[4]);
                score = Integer.valueOf(conData[5]);
                System.out.println("Multicast = " + multicastIp + ":" + multicastPort + ". TCP = " +
                                   tcpIp + ": " + tcpPort + ". Score =" + score + " received by " + usr);
            } else {
                System.out.println(usr + " could not login");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return loginSuccess;
    }

    public int[] listenMulticast () {
        MulticastSocket mcastSocket = null;
        int fromServer = -1, round = -1, newMoleTile = -1;

        try {
            InetAddress group = InetAddress.getByName(multicastIp); // destination multicast group
            mcastSocket = new MulticastSocket(multicastPort);
            mcastSocket.joinGroup(group);
            byte[] buffer = new byte[1000];

            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
            mcastSocket.receive(messageIn);

            byte[] mcastData = messageIn.getData();
            String msg = new String(Arrays.copyOfRange(mcastData, 1, mcastData[0]+1)).trim();
            round = mcastData[mcastData[0] + 1];
            newMoleTile = mcastData[mcastData[0] + 2];

            // Validates message source
            if(msg.equals("server")) {
                fromServer = 1;
                System.out.println(usr + " received mole at " + String.valueOf(newMoleTile)
                                       + " on round " + String.valueOf(round) + " from server");
            }

            mcastSocket.leaveGroup(group);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if(mcastSocket != null) mcastSocket.close();
        }

        int[] out = {fromServer, round, newMoleTile};
        return out;
    }

    /*public void updateGrid () {
        try {
            InetAddress group = InetAddress.getByName(multicastIp);
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
    */

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
            System.out.println(usr + " received response: " + Arrays.toString(hit));

            if(hit[0].equals("hit")) {
                reqApproved = true;
                score++;
                System.out.println(usr + " received a hit. New score is " + score);
            } else if(hit[0].equals("win")) {
                // TODO
                System.out.println(usr + " received a win");
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

    public String getUsr() {
        return usr;
    }

    public String getPwd() {
        return pwd;
    }

    public int getScore() {
        return score;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /*public static void main(String[] args) {
        Client client = new Client("User");
        client.start();
    }*/
}
