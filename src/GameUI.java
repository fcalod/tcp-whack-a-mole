package src;

import javax.swing.*;
import java.awt.*;

public class GameUI {
    LoginWindow loginWindow;
    GameWindow gameWindow;

    private class LoginWindow {
        public int width = 120;
        public int height = 140;
        JFrame frame;
        JPanel panel;
        ImageIcon moleIcon;
        ImageIcon treeIcon;

        public LoginWindow() {
            // Creates board container
            panel = new JPanel(new GridLayout(3,3));
            panel.setPreferredSize(new Dimension(900,950));

            //moleIcon = GameUI.createImageIcon("assets/awakkate_1.png", 180, 180);
            moleIcon = GameUI.createImageIcon("assets/mole_over_tree_1.png", 0, 0);
            //moleIcon = GameUI.createImageIcon("assets/mole_over_tree_2.png", 0, 0);
            treeIcon = GameUI.createImageIcon("assets/tree.png", 0, 0);
            //background = GameUI.createImageIcon("assets/grass.jpg");

            // Creates game window
            frame = new JFrame("Wakk-a-Mole Login");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

    }

    private class GameWindow {
        public int width = 120;
        public int height = 140;
        JFrame frame;
        JPanel panel;
        JButton[] board;
        ImageIcon moleIcon;
        ImageIcon treeIcon;
        //ImageIcon background;

        public GameWindow() {
            // Creates board container
            panel = new JPanel(new GridLayout(3,3));
            panel.setPreferredSize(new Dimension(900,950));

            //moleIcon = GameUI.createImageIcon("assets/awakkate_1.png", 180, 180);
            moleIcon = GameUI.createImageIcon("assets/mole_over_tree_1.png", 0, 0);
            //moleIcon = GameUI.createImageIcon("assets/mole_over_tree_2.png", 0, 0);
            treeIcon = GameUI.createImageIcon("assets/tree.png", 0, 0);
            //background = GameUI.createImageIcon("assets/grass.jpg");

            // Adds tiles
            board = new JButton[9];

            for (int i = 0; i < 9; i++) {
                JButton tile = new JButton();
                tile.setFocusable(false);
                tile.setIcon(treeIcon);
                board[i] = tile;
                panel.add(tile);
            }

            //panel.getComponent(2);

            // Creates game window
            frame = new JFrame("Wakk-a-Mole");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
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

    public static void main(String[] args) {
        GameUI game = new GameUI();
        int[] moles = {0, 1, 0, 1, 0, 0, 0, 0, 0};
        game.gameWindow.updateMoles(moles);
    }
}
