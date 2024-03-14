package src;

import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

public class ClientDeployer {
    public static void writeResults(Game[] games, double[] data) throws IOException {
        String csvFile = "stress_" + games.length + ".csv";
        CSVWriter cw = new CSVWriter(new FileWriter(csvFile));
        String[] line = new String[data.length];

        for(int i = 0; i < data.length; i++)
            line[i] = Double.toString(data[i]);

        cw.writeNext(line);
        cw.close();
    }

    public static void main(String[] args) {
        int NUM_CLIENTS = 500; // 50, 100, 150, 500
        int NUM_TESTS = 1;
        double avgLoginTime = 0.0, avgHitRespTime = 0.0, loginDev = 0.0, hitDev = 0.0, avgLoginScsRate = 0.0;
        double[] stdDev = new double[NUM_TESTS];

        Game[] games = new Game[NUM_CLIENTS];
        Server server;// = new Server("Stress", NUM_TESTS);

        for(int i = 0; i < NUM_TESTS; i++) {
            server = new Server("Stress", NUM_TESTS);
            server.start();

            for(int j = 0; j < NUM_CLIENTS; j++) {
                games[j] = new Game("Stress");
                games[j].start();
            }

            // Waits for game to end
            try {
                server.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            /*
            // Sleeps so server finishes first
            try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }*/

            System.out.println("======= TEST " + i + " =======");

            /*for(int j = 0; j < NUM_CLIENTS; j++) {
                System.out.println(games[j].client.getUsr());
                System.out.println("Login time: " + games[j].client.getLoginRespTime() + "s");
                System.out.println("Avg hit resp time: " + games[j].client.getAvgHitRespTime() + "s");
            }*/

            double loginScsCount = 0.0, loginFailCount = 0.0, loginVar = 0.0, hitVar = 0.0;

            for(Game game: games) {
                avgLoginTime += game.client.getLoginRespTime();
                avgHitRespTime += game.client.getAvgHitRespTime();
                loginScsCount += game.client.getLoginCounter()[0];
                loginFailCount += game.client.getLoginCounter()[1];
            }

            avgLoginTime /= NUM_CLIENTS;
            avgHitRespTime /= NUM_CLIENTS;
            avgLoginScsRate += (loginScsCount + 1) / (loginFailCount + 1);

            for(Game game: games) {
                loginVar += Math.pow(game.client.getLoginRespTime() - avgLoginTime, 2);
                hitVar += Math.pow(game.client.getLoginRespTime() - avgHitRespTime, 2);
            }

            loginDev += Math.sqrt(loginVar/ NUM_CLIENTS);
            hitDev += Math.sqrt(hitVar / NUM_CLIENTS);
        }

        avgLoginTime /= NUM_TESTS;
        avgHitRespTime /= NUM_TESTS;
        avgLoginScsRate /= NUM_TESTS;
        loginDev /= NUM_TESTS;
        hitDev /= NUM_TESTS;
        double[] data = {avgLoginTime, loginDev, avgHitRespTime, hitDev, avgLoginScsRate};

        // Write results to file
        try {
            writeResults(games, data);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        System.out.println("DONE");
    }
}
