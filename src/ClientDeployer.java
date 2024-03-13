package src;

import com.sun.tools.javac.Main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import javax.sound.sampled.*;

public class ClientDeployer {
    public static String formatCSV(String[] data) {
        // TODO: regresar el renglón que se va a escribir en el csv, incluyendo las comas
        // Considerar que un cliente puede no tener tiempos de respuesta en el juego (si nunca le atinó)
        return "";
    }

    public static void writeResults(Game[] games, int id) throws IOException {
        File csvOutputFile = new File("stress/" + games.length + "clients_" + id + ".csv");

        for(Game g: games) {
            Client c = g.client;


        }

        // TODO: escribir cada línea en un archivo. El código marca error. Lo saqué de aquí
        // https://www.baeldung.com/java-csv
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            /*dataLines.stream()
                    .map(this::formatCSV)
                    .forEach(pw::println);*/
        }
    }

    public static void main(String[] args) {
        int NUM_CLIENTS = 2; // 50, 100, 500, 1000
        int NUM_TESTS = 1;

        for(int i = 0; i < NUM_TESTS; i++) {
            Game[] games = new Game[NUM_CLIENTS];
            Server server = new Server("Stress");
            server.start();

            for(int j = 0; j < NUM_CLIENTS; j++) {
                games[j] = new Game("Stress");
                games[j].start();
            }

            boolean gameOver = false;

            while(!gameOver)
                gameOver = server.isGameOver();

            // Sleeps so server finishes first
            try{ Thread.sleep(500); } catch (InterruptedException e){ e.printStackTrace(); }

            for(int j = 0; j < NUM_CLIENTS; j++) {
                System.out.println(games[j].client.getUsr());
                System.out.println("Login time: " + games[j].client.getLoginRespTime() + "s");
                System.out.println("Avg hit resp time: " + games[j].client.getAvgHitRespTime() + "s");
            }

            // Write results to file
            /*try {
                writeResults(games, i);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }*/
        }
    }
}
