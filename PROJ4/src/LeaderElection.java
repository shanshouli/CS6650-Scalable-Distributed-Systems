import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * if no server with a higher ID is alive, then I'm the leader.
 */
public class LeaderElection extends Thread {
    private final int serverId;
    private final PaxosServer parentServer;
    private final AtomicBoolean running;
    private final Random random;

    public LeaderElection(int serverId, PaxosServer parentServer) {
        this.serverId = serverId;
        this.parentServer = parentServer;
        this.running = new AtomicBoolean(true);
        this.random = new Random();
    }

    @Override
    public void run() {
        System.out.println("LeaderElection " + serverId + " STARTED.");
        // random lifetime
        long lifetimeMillis = 5000 + random.nextInt(5000);
        long startTime = System.currentTimeMillis();

        while (running.get()) {
            if (System.currentTimeMillis() - startTime > lifetimeMillis) {
                System.out.println("LeaderElection " + serverId + " FAILING.");
                running.set(false);
                break;
            }

            try {
                // Check leadership every 100ms
                Thread.sleep(100);
            } catch (InterruptedException e) {
                running.set(false);
                break;
            }

            // scan all servers to see if any have ID higher than ours and is alive
            PaxosServer[] all = parentServer.getAllServers();
            boolean foundHigherAlive = false;
            for (PaxosServer s : all) {
                if (s.getServerId() > serverId && s.isAliveServer()) {
                    foundHigherAlive = true;
                    break;
                }
            }
            // If we did NOT find a higher ID that is alive, we become leader
            parentServer.setLeader(!foundHigherAlive);
        }

        System.out.println("LeaderElection " + serverId + " STOPPED.");
    }

    /**
     * shut down method
     */
    public void shutdownLeaderElection() {
        running.set(false);
        this.interrupt();
    }
}
