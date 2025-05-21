import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Learner receives ACCEPTED messages from Acceptors
 */
public class Learner extends Thread {
    private final int serverId;
    private final PaxosServer parentServer;
    private final AtomicBoolean running;
    private final Random random;

    // for each instanceNumber, record how many Acceptors accepted it
    private final Map<Integer, Map<String, Integer>> acceptedCountPerValue = new ConcurrentHashMap<>();

    // Once chosen, store the final result here to avoid reapplying it
    private final Map<Integer, String> chosenValues = new ConcurrentHashMap<>();

    public Learner(int serverId, PaxosServer parentServer) {
        this.serverId = serverId;
        this.parentServer = parentServer;
        this.running = new AtomicBoolean(true);
        this.random = new Random();
    }

    @Override
    public void run() {
        System.out.println("Learner " + serverId + " STARTED.");
        // random lifetime
        long lifetimeMillis = 5000 + random.nextInt(5000);
        long startTime = System.currentTimeMillis();

        while (running.get()) {
            if (System.currentTimeMillis() - startTime > lifetimeMillis) {
                System.out.println("Learner " + serverId + " FAILING.");
                running.set(false);
                break;
            }

            // Poll for ACCEPTED messages
            PaxosMessage msg = parentServer.pollLearnerQueue();
            if (msg != null && msg.getType() == PaxosMessageType.ACCEPTED) {
                onAccepted(msg);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                running.set(false);
            }
        }
        System.out.println("Learner " + serverId + " STOPPED.");
    }

    /**
     * Called upon receiving an ACCEPTED message from an Acceptor
     */
    private synchronized void onAccepted(PaxosMessage msg) {
        int instanceNumber = msg.getInstanceNumber();
        String val = msg.getValue();

        acceptedCountPerValue.putIfAbsent(instanceNumber, new HashMap<>());
        Map<String, Integer> mapForInstance = acceptedCountPerValue.get(instanceNumber);

        int newCount = mapForInstance.getOrDefault(val, 0) + 1;
        mapForInstance.put(val, newCount);

        // If a majority has accepted the same value, it's chosen
        if (newCount > (parentServer.getNumServers() / 2)) {
            if (!chosenValues.containsKey(instanceNumber)) {
                chosenValues.put(instanceNumber, val);
                System.out.println("Learner " + serverId + " CHOSEN instance "
                        + instanceNumber + ": " + val);

                // Apply to local KV-store
                parentServer.applyChosenValue(val);
            }
        }
    }

    /**
     * shut down method
     */
    public void shutdownLearner() {
        running.set(false);
        this.interrupt();
    }
}
