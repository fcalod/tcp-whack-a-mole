package src;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class TestUI {
    public static void main(String[] args) {
        //LoginWindow loginWindow = new LoginWindow();
        GameWindow gameWindow = new GameWindow();
        gameWindow.updateMole(4);
    }
}

class LoginWindow extends JFrame {
    public int width = 500;
    public int height = 500;
    JPanel panel;
    JTextField usrField;
    JTextField pwdField;
    JLabel errLabel;
    JButton loginBtn;

    public LoginWindow() {
        super("Wakk-a-Mole Login");
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
        this.setVisible(true);
    }

    public void pwdError() {
        errLabel.setText("Incorrect password");
    }
}

class GameWindow extends JFrame {
    public int width = 120;
    public int height = 140;
    JPanel panel;
    JButton[] board;
    JLabel score; // TODO: mostrar el puntaje
    ImageIcon treeIcon;
    ImageIcon moleIcon;
    ImageIcon splatIcon;
    ImageIcon missIcon;

    public GameWindow() {
        super("Wakk-a-Mole");
        // Creates board container
        panel = new JPanel(new GridLayout(3,3));
        panel.setPreferredSize(new Dimension(900,950));

        treeIcon = Game.createImageIcon("assets/tree.png", 0, 0);
        moleIcon = Game.createImageIcon("assets/mole_over_tree_1.png", 0, 0);
        splatIcon = Game.createImageIcon("assets/splat_over_tree.png", 0, 0);
        missIcon = Game.createImageIcon("assets/cross_over_tree.png", 0, 0);

        // Adds tiles
        board = new JButton[9];

        for (int i = 0; i < 9; i++) {
            JButton tile = new JButton();
            tile.setFocusable(false);
            tile.setIcon(treeIcon);
            tile.setName(String.valueOf(i)); // stores the tile's number

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
        this.setVisible(true);
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
            }

            panel.add(board[i]);
        }
    }

    public void updateScore(int moleTile) {
        score.setText( String.valueOf(Integer.valueOf(score.getText()) + 1) );
        board[moleTile].setIcon(splatIcon);
    }

    public void wrongMole(int clickedTile) {
        board[clickedTile].setIcon(splatIcon);
    }
}
