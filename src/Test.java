package src;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.Paths;

public class Test extends JPanel {
    public Test() {
        super(new GridLayout(3,1));  //3 rows, 1 column
        JLabel label1, label2, label3;

        ImageIcon icon = createImageIcon("assets/grass.jpg");
        //ImageIcon icon = new ImageIcon("assets/grass.jpg");

        //Create the first label.
        label1 = new JLabel(icon, JLabel.CENTER);
        //Set the position of its text, relative to its icon:
        label1.setVerticalTextPosition(JLabel.BOTTOM);
        label1.setHorizontalTextPosition(JLabel.CENTER);

        //Create the other labels.
        //label2 = new JLabel("Text-Only Label");
        //label3 = new JLabel(icon);

        //Create tool tips, for the heck of it.
        //label1.setToolTipText("A label containing both image and text");
        //label2.setToolTipText("A label containing only text");
        //label3.setToolTipText("A label containing only an image");

        //Add the labels.
        add(label1);
        //add(label2);
        //add(label3);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = Test.class.getResource(path);

        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new Test());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        createAndShowGUI();

        /*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                //UIManager.put("swing.boldMetal", Boolean.FALSE);

                createAndShowGUI();
            }
        });*/
    }
}
