package src;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Game {
    Client client;
    LoginWindow loginWindow;
    GameWindow gameWindow;

    public Game() {
        loginWindow = new LoginWindow(this);
        gameWindow = new GameWindow(this);
    }

    public void goToLogin() {
        loginWindow.setVisible(true);
        gameWindow.setVisible(false);
    }

    public void goToGame() {
        loginWindow.setVisible(false);
        gameWindow.setVisible(true);
    }

    public void tryLogin(String usr, String pwd) {
        boolean loginSuccess = client.tryLogin(usr, pwd);

        if(loginSuccess) {
            client.setUsr(usr);
            client.setPwd(pwd);
            goToGame();
        } else {
            loginWindow.pwdError();
        }
    }

    private class LoginWindow extends JFrame {
        protected Game game;
        protected static String loginIp = "localhost";//"192.168.10.10";
        protected static int loginPort = 5050;
        public int width = 500;
        public int height = 500;
        JPanel panel;
        ImageIcon moleIcon;
        ImageIcon treeIcon;
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

            //moleIcon = Game.createImageIcon("assets/awakkate_1.png", 180, 180);
            moleIcon = Game.createImageIcon("assets/mole_over_tree_1.png", 0, 0);
            //moleIcon = Game.createImageIcon("assets/mole_over_tree_2.png", 0, 0);
            treeIcon = Game.createImageIcon("assets/tree.png", 0, 0);
            //background = Game.createImageIcon("assets/grass.jpg");

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

        public void pwdError() {
            errLabel.setText("Incorrect password");
        }
    }

    private class GameWindow extends JFrame {
        Game game;
        public int width = 120;
        public int height = 140;
        JPanel panel;
        JButton[] board;
        ImageIcon moleIcon;
        ImageIcon treeIcon;
        //ImageIcon background;

        public GameWindow(Game game) {
            super("Wakk-a-Mole");
            this.game = game;
            // Creates board container
            panel = new JPanel(new GridLayout(3,3));
            panel.setPreferredSize(new Dimension(900,950));

            //moleIcon = Game.createImageIcon("assets/awakkate_1.png", 180, 180);
            moleIcon = Game.createImageIcon("assets/mole_over_tree_1.png", 0, 0);
            //moleIcon = Game.createImageIcon("assets/mole_over_tree_2.png", 0, 0);
            treeIcon = Game.createImageIcon("assets/tree.png", 0, 0);
            //background = Game.createImageIcon("assets/grass.jpg");

            // Adds tiles
            board = new JButton[9];

            for (int i = 0; i < 9; i++) {
                JButton tile = new JButton();
                tile.setFocusable(false);
                tile.setName(String.valueOf(i));
                tile.setIcon(treeIcon);
                board[i] = tile;
                panel.add(tile);
            }

            //panel.getComponent(2);

            // Creates game window
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setResizable(false);
            this.add(panel);
            this.pack();
            this.setLocationRelativeTo(null);
        }

        public void updateMoles(int[] moles) {
            panel.removeAll();

            for(int i = 0; i < 9; i++) {
                if(moles[i] == 1) {
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
                }

                panel.add(board[i]);
            }
        }
    }

    protected static ImageIcon createImageIcon(String path, int width, int height) {
        java.net.URL imgURL = Test.class.getResource(path);

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

    /*public static void main(String[] args) {
        Game game = new Game();
        game.goToLogin();
        //game.goToGame();
        //game.loginWindow.pwdError();

        //int[] moles = {0, 1, 0, 1, 0, 0, 0, 0, 0};
        //game.gameWindow.updateMoles(moles);

    }*/
}
