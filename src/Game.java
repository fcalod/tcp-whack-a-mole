package src;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Game extends Thread {
    protected Client client;
    protected LoginWindow loginWindow;
    protected GameWindow gameWindow;
    protected UpdateListener updateListener;
    protected static final int WAIT_BETWEEN_ROUNDS = 5000;
    protected static final int STRESS_WAIT_CLICK = 50; // Waits before clicking again
    protected String mode; // "User" or "Stress"
    protected int round = 0;
    protected int moleTile = -1;
    protected String winner = "";

    public Game(String mode) {
        this.mode = mode;
        client = new Client(mode);
        client.start();
        updateListener = new UpdateListener(this);

        if(mode.equals("User")) {
            loginWindow = new LoginWindow(this);
            gameWindow = new GameWindow(this);
            goToLogin();
        } else if(mode.equals("Stress")) {
            //tryLoginStress();
        }
    }

    public void goToLogin() {
        loginWindow.setVisible(true);
        gameWindow.setVisible(false);
    }

    public void goToGame() {
        loginWindow.setVisible(false);
        gameWindow.setVisible(true);
        gameWindow.setTitle("Wakk-a-Mole | Score: " + client.getScore());
    }

    public void tryLogin(String usr, String pwd) {
        if(usr.isEmpty()) {
            loginWindow.usrError();
            return;
        }

        boolean loginSuccess = client.tryLogin(usr, pwd);

        if(loginSuccess) {
            client.setUsr(usr);
            client.setPwd(pwd);
            goToGame();
            updateListener.start();
            //loop();
        } else {
            loginWindow.pwdError();
        }
    }

    public void tryLoginStress() {
        // Generates a random username and empty password
        String usr = generateRandomString(30), pwd = "";

        boolean loginSuccess = client.tryLogin(usr, pwd);

        if(loginSuccess) {
            client.setUsr(usr);
            client.setPwd(pwd);
            updateListener.start();
            hitMoleStress();
        } else {
            tryLoginStress();
        }
    }

    private static String generateRandomString(int length){
        Random rand = new Random();
        return rand.ints(48,122)
                .filter(i-> (i<57 || i>65) && (i <90 || i>97))
                .mapToObj(i -> (char) i)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public void updateMole(int newMoleTile) {
        moleTile = newMoleTile;

        if(mode.equals("User"))
            gameWindow.updateMole(newMoleTile);
    }

    public void hitMole(int clickedTile) {
        if(clickedTile == moleTile) {
            boolean reqApproved = client.reqScoreUpdate(round);

            if(reqApproved) {
                gameWindow.updateScore(moleTile);
                GameWindow.playSound("ouch.wav");

                // If player won, end game here
                if(winner.equals(client.getUsr())) {
                    endGame();
                    GameWindow.playSound("ganaste.wav");
                }
            } else {
                //GameWindow.playSound("ganaste.wav");
            }
        } else {
            gameWindow.wrongMole(clickedTile);
        }
    }

    public void hitMoleStress() {
        while(winner.isEmpty()) {
            Random rand = new Random();
            int clickedTile = rand.nextInt(9); // Clicks a tile at random

            if(clickedTile == moleTile) {
                boolean reqApproved = client.reqScoreUpdate(round);

                if(reqApproved) {
                    // If player won, end game here
                    if(winner.equals(client.getUsr()))
                        endGameStress();
                }
            }

            try{ Thread.sleep(STRESS_WAIT_CLICK); } catch (InterruptedException e){ e.printStackTrace(); }
        }
    }

    public void endGame() {
        gameWindow.showWinner();
        // Waits before starting another round
        try{ Thread.sleep(WAIT_BETWEEN_ROUNDS); } catch (InterruptedException e){ e.printStackTrace(); }
        winner = "";
        client.setScore(0);
        gameWindow.setTitle("Wakk-a-Mole | Score: 0");
    }

    public void endGameStress() {
        double respTimeSum = client.getHitRespTimes().stream().reduce(0.0, Double::sum);
        client.setAvgHitRespTime(respTimeSum / client.getHitRespTimes().size());
        /*// Waits before starting another round
        try{ Thread.sleep(WAIT_BETWEEN_ROUNDS); } catch (InterruptedException e){ e.printStackTrace(); }
        winner = "";
        client.setScore(0);*/
    }

    public String getWinner() { return winner; }

    public void setRound(int round) { this.round = round; }

    public void setWinner(String winner) { this.winner = winner; }

    private class LoginWindow extends JFrame {
        protected Game game;
        public int width = 500;
        public int height = 500;
        JPanel panel;
        JTextField usrField;
        JTextField pwdField;
        JLabel errLabel;
        JButton loginBtn;

        public LoginWindow(Game game) {
            super("Wakk-a-Mole Login");
            this.game = game;
            // Creates login form
            panel = new JPanel(new GridLayout(6, 1));
            panel.setPreferredSize(new Dimension(width, height));

            JLabel usrLabel = new JLabel("Username");
            JLabel pwdLabel = new JLabel("Password");
            errLabel = new JLabel();
            usrField = new JTextField();
            pwdField = new JTextField();
            loginBtn = new JButton("Login");

            Font font = new Font("SansSerif", Font.PLAIN, 25);
            Font errFont = new Font("SansSerif", Font.PLAIN, 18);
            Border border = BorderFactory.createMatteBorder(4, 16, 4, 16, Color.lightGray);
            usrField.setBorder(border);
            pwdField.setBorder(border);
            usrLabel.setFont(font);
            pwdLabel.setFont(font);
            usrField.setFont(font);
            pwdField.setFont(font);
            loginBtn.setFont(font);
            errLabel.setFont(errFont);
            errLabel.setForeground(new Color(200, 0, 0));
            usrField.setPreferredSize(new Dimension(300, 80));
            pwdField.setPreferredSize(new Dimension(300, 80));
            loginBtn.setPreferredSize(new Dimension(300, 50));
            loginBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    game.tryLogin(usrField.getText(), pwdField.getText());
                }
            });

            panel.add(usrLabel);
            panel.add(usrField);
            panel.add(pwdLabel);
            panel.add(pwdField);
            panel.add(errLabel);
            panel.add(loginBtn);

            // Creates login window
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setResizable(false);
            this.add(panel);
            this.pack();
            this.setLocationRelativeTo(null);
        }

        public void usrError() {
            errLabel.setText("Empty username");
        }

        public void pwdError() {
            errLabel.setText("Incorrect password");
        }
    }

    protected static ImageIcon createImageIcon(String path, int width, int height) {
        java.net.URL imgURL = Game.class.getResource(path);

        if(imgURL != null) {
            if(width == 0 || height == 0)
                return new ImageIcon(imgURL);
            else {
                ImageIcon icon0 = new ImageIcon(imgURL);
                Image img = icon0.getImage();
                Image scaledImg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private class GameWindow extends JFrame implements WindowListener {
        Game game;
        public int width = 120;
        public int height = 140;
        JPanel panel;
        JButton[] board;
        JLabel scoreLabel; // TODO: quitar
        ImageIcon treeIcon;
        ImageIcon moleIcon;
        ImageIcon splatIcon;
        ImageIcon missIcon;

        public GameWindow(Game game) {
            super("Wakk-a-Mole");
            this.game = game;
            // Creates board container
            panel = new JPanel(new GridLayout(3,3));
            panel.setPreferredSize(new Dimension(900,950));

            treeIcon = Game.createImageIcon("assets/tree.png", 0, 0);
            moleIcon = Game.createImageIcon("assets/mole_over_tree_1.png", 0, 0);
            splatIcon = Game.createImageIcon("assets/splat_over_tree.png", 0, 0);
            missIcon = Game.createImageIcon("assets/cross_over_tree.png", 0, 0);
            scoreLabel = new JLabel("0");

            // Adds tiles
            board = new JButton[9];

            for (int i = 0; i < 9; i++) {
                JButton tile = new JButton();
                tile.setFocusable(false);
                tile.setIcon(treeIcon);
                tile.setName(String.valueOf(i)); // stores the tile's number
                tile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        game.hitMole(Integer.valueOf(tile.getName()));
                    }
                });

                board[i] = tile;
                panel.add(tile);
            }

            // Creates game window
            //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Plays a sound on close
            this.setResizable(false);
            this.add(panel);
            this.pack();
            this.setLocationRelativeTo(null);
            this.addWindowListener(this);
        }

        public void updateMole(int moleTile) {
            panel.removeAll();

            for(int i = 0; i < 9; i++) {
                if(i == moleTile) {
                    board[i].setIcon(moleIcon);
                /*JButton tile = new JButton();
                tile.setFocusable(false);
                BufferedImage combined = new BufferedImage(treeIcon.getIconWidth(), treeIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                // paint both images, preserving the alpha channels
                Graphics g = combined.getGraphics();
                g.drawImage(treeIcon.getImage(), 0, 0, null);
                g.drawImage(moleIcon.getImage(), 0, 0, null);
                tile.setIcon(new ImageIcon(combined));
                board[i] = tile;*/
                } else {
                    board[i].setIcon(treeIcon);
                }

                panel.add(board[i]);
            }

            panel.repaint();
            playSound("hey.wav");
        }

        public void updateScore(int moleTile) {
            board[moleTile].setIcon(splatIcon);
            panel.repaint();
            //scoreLabel.setText( String.valueOf(Integer.valueOf(scoreLabel.getText()) + 1) );
            this.setTitle("Wakk-a-Mole | Score: " + game.client.getScore());
        }

        public void wrongMole(int clickedTile) {
            board[clickedTile].setIcon(missIcon);
        }

        public void showWinner() {
            String winnerMsg;

            if(game.getWinner().equals(game.client.getUsr())) {
                winnerMsg = " | ¡Ganaste!";
                playSound("ganaste.wav");
            } else {
                winnerMsg = " | Ganó " + winner;
                playSound("perdiste.wav");
            }

            this.setTitle("Wakk-a-Mole | Score: " + game.client.getScore() + winnerMsg);
        }

        public static synchronized void playSound(final String url) {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                        ClientDeployer.class.getResourceAsStream("assets/" + url));
                clip.open(inputStream);
                clip.start();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        public void windowOpened(WindowEvent e) {}

        public void windowClosing(WindowEvent e) {
            playSound("adios.wav");
            System.exit(0);
        }

        public void windowClosed(WindowEvent e) {}

        public void windowIconified(WindowEvent e) {}

        public void windowDeiconified(WindowEvent e) {}

        public void windowActivated(WindowEvent e) {}

        public void windowDeactivated(WindowEvent e) {}
    }

    private class UpdateListener extends Thread {
        Game game;

        public UpdateListener(Game game) {
            this.game = game;
        }

        @Override
        public void run() {
            while(true) {
                String[] updateData = game.client.listenMulticast();
                String msg = updateData[0], winner = updateData[3];

                // If msg came from server, update mole and round
                if(msg.equals("serverUpdate")) {
                    int round = Integer.parseInt(updateData[1]), newMoleTile = Integer.parseInt(updateData[2]);
                    game.setRound(round);
                    game.updateMole(newMoleTile);
                } else if(msg.equals("serverWinner")) {
                    game.setWinner(winner);

                    // Ends the game if player lost; if player won, the TCP listener ends it
                    if(!winner.equals(game.client.getUsr()))
                        if(game.mode.equals("User"))
                            game.endGame();
                        else if(game.mode.equals("Stress"))
                            game.endGameStress();
                }
            }
        }
    }

    @Override
    public void run() {
        if(mode.equals("Stress"))
            tryLoginStress();
    }

    public static void main(String[] args) {
        Game game = new Game("User");
    }
}
