public class Client {
    public static void main(String[] args) throws Exception {
        int numServers = 5;
        PaxosServer[] servers = new PaxosServer[numServers];

        // Create each server
        for (int i = 0; i < numServers; i++) {
            servers[i] = new PaxosServer(i, numServers);
        }

        // Let each server know about the others
        for (int i = 0; i < numServers; i++) {
            servers[i].setAllServers(servers);
        }

        // Start the Paxos threads
        for (int i = 0; i < numServers; i++) {
            servers[i].startAllThreads();
        }

        // Give time to elect a leader
        Thread.sleep(3000);

        System.out.println("Pre-populate the key-value store\n");
        // 5 PUT operations
        servers[0].put("MacBook", 200);
        servers[0].put("AppleWatch", 500);
        servers[0].put("iPad", 300);
        servers[0].put("VisionPro", 50);
        servers[0].put("HomePod", 100);

        // Wait to finish
        Thread.sleep(1000);

        System.out.println("\n Delete items from the key-value store \n");
        // 5 DELETE operations
        servers[0].delete("MacBook", 200);
        servers[0].delete("AppleWatch", 500);
        servers[0].delete("iPad", 300);
        servers[0].delete("VisionPro", 50);
        servers[0].delete("HomePod", 100);

        // Wait
        Thread.sleep(1000);

        System.out.println("\n Retrieve items from the key-value store \n");
        // 5 GET operations
        System.out.println("MacBook: " + servers[0].get("MacBook"));
        System.out.println("AppleWatch: " + servers[0].get("AppleWatch"));
        System.out.println("iPad: " + servers[0].get("iPad"));
        System.out.println("VisionPro: " + servers[0].get("VisionPro"));
        System.out.println("HomePod: " + servers[0].get("HomePod"));

        Thread.sleep(2000);

        // Print the final store contents on each server
        System.out.println("\n Final state on each server ");
        for (int i = 0; i < numServers; i++) {
            System.out.println("Server " + i +
                    ": MacBook=" + servers[i].get("MacBook") +
                    ", AppleWatch=" + servers[i].get("AppleWatch") +
                    ", iPad=" + servers[i].get("iPad") +
                    ", VisionPro=" + servers[i].get("VisionPro") +
                    ", HomePod=" + servers[i].get("HomePod")
            );
        }

        // Shut down all servers so the threads can stop and the program exits cleanly
        shutdownAll(servers);
        System.out.println("\nAll servers shut down.");
    }

    /**
     * Helper method to stop all threads on each server.
     */
    private static void shutdownAll(PaxosServer[] servers) {
        for (PaxosServer s : servers) {
            s.shutdown();
        }
    }
}
