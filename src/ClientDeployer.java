package src;

// For testing purposes
public class ClientDeployer {

    public static void main(String[] args) {
        int NUM_CLIENTS = 100;

        for(int i = 0; i < NUM_CLIENTS; i++) {
            Game game = new Game("Stress");
            //Client client = new Client("Stress");
            //client.start();
        }
    }
}
