package src;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

public class Game {
    protected Client client;
    protected LoginWindow loginWindow;
    protected GameWindow gameWindow;
    protected UpdateListener updateListener;
    protected static final int WAIT_BETWEEN_ROUNDS = 5000;
    protected String mode;
    protected int round = 0;
    protected int moleTile = -1;
    protected String winner = "";

    public Game(String mode) {
        this.mode = mode;
        client = new Client(mode);
        client.start();
        loginWindow = new LoginWindow(this);
        gameWindow = new GameWindow(this);
        updateListener = new UpdateListener(this);
        goToLogin();
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

    public void updateMole(int newMoleTile) {
        moleTile = newMoleTile;
        gameWindow.updateMole(newMoleTile);
    }

    public void hitMole(int clickedTile) {
        if(clickedTile == moleTile) {
            boolean reqApproved = client.reqScoreUpdate(round);

            if(reqApproved) {
                gameWindow.updateScore(moleTile);

                // If player won, end game here
                if(winner.equals(client.getUsr()))
                    endGame();
            } else {
                //TODO
            }


        } else {
            gameWindow.wrongMole(clickedTile);
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

    public String getWinner() {
        return winner;
    }

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

    private class GameWindow extends JFrame {
        Game game;
        public int width = 120;
        public int height = 140;
        JPanel panel;
        JButton[] board;
        JLabel scoreLabel; // TODO: mostrar el puntaje
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

            //panel.getComponent(2);

            // Creates game window
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setResizable(false);
            this.add(panel);
            this.pack();
            this.setLocationRelativeTo(null);
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

            if(game.getWinner().equals(game.client.getUsr()))
                winnerMsg = " | ¡Ganaste!";
            else
                winnerMsg = " | Ganó " + winner;

            this.setTitle("Wakk-a-Mole | Score: " + game.client.getScore() + winnerMsg);
        }
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
                        game.endGame();
                }
            }
        }
    }

    public static void main(String[] args) {
        Game game = new Game("User");
    }
}
