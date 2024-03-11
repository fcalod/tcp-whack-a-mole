package src;

import com.sun.tools.javac.Main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import javax.sound.sampled.*;

public class ClientDeployer {
    public static String convertToCSV(String[] data) {
        return "";
    }

    public static void writeResults(Game[] games, int id) throws IOException {
        File csvOutputFile = new File("stress/" + games.length + "clients_" + id + ".csv");

        for(Game g: games) {
            Client c = g.client;


        }

        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            /*dataLines.stream()
                    .map(this::formatCSV)
                    .forEach(pw::println);*/
        }
    }

    public static void main(String[] args) {
        /*int NUM_CLIENTS = 10; // 50, 100, 500, 1000
        Game[] games = new Game[NUM_CLIENTS];

        for(int i = 0; i < NUM_CLIENTS; i++)
            games[i] = new Game("Stress");

        // Write results to file
        try {
            writeResults(games, 0);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }*/
    }


}
