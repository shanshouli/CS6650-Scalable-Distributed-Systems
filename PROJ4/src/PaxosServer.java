import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * represents one replica in the Paxos system.
 */
public class PaxosServer {
    private final int serverId;
    private final int numServers;
    private PaxosServer[] allServers; // references to all servers in the cluster

    // four role threads:
    private Acceptor acceptorThread;
    private Proposer proposerThread;
    private Learner learnerThread;
    private LeaderElection leaderElectionThread;

    // Paxos inbound queues
    private final Queue<PaxosMessage> acceptorQueue = new ConcurrentLinkedQueue<>();
    private final Queue<PaxosMessage> proposerQueue = new ConcurrentLinkedQueue<>();
    private final Queue<PaxosMessage> learnerQueue = new ConcurrentLinkedQueue<>();

    // value are we proposing at each instanceNumber
    private final Map<Integer, String> pendingValues = new ConcurrentHashMap<>();

    // the local Key-Value store
    private final Map<String, Integer> store = new ConcurrentHashMap<>();

    // is this server recognized as the leader or not
    private volatile boolean isLeader = false;

    // "alive" status for leader election
    private final AtomicBoolean alive;

    public PaxosServer(int serverId, int numServers) {
        this.serverId = serverId;
        this.numServers = numServers;
        this.alive = new AtomicBoolean(true);
    }


    // Starting threads
    public void startAllThreads() {
        startAcceptor();
        startProposer();
        startLearner();
        startLeaderElection();
    }

    /**
     * Called periodically in the main driver to see if any threads have failed
     * and, if so, restart them with fresh state.
     */
    public void maybeRestartThreads() {
        if (acceptorThread == null || !acceptorThread.isAlive()) {
            System.out.println("Server " + serverId + " Restarting Acceptor.");
            startAcceptor();
        }
        if (proposerThread == null || !proposerThread.isAlive()) {
            System.out.println("Server " + serverId + " Restarting Proposer.");
            startProposer();
        }
        if (learnerThread == null || !learnerThread.isAlive()) {
            System.out.println("Server " + serverId + " Restarting Learner.");
            startLearner();
        }
        if (leaderElectionThread == null || !leaderElectionThread.isAlive()) {
            System.out.println("Server " + serverId + " Restarting LeaderElection.");
            startLeaderElection();
        }
    }

    private void startAcceptor() {
        acceptorThread = new Acceptor(serverId, this);
        acceptorThread.start();
    }

    private void startProposer() {
        if (!(proposerThread instanceof Proposer)) {
            proposerThread = new Proposer(serverId, this);
            proposerThread.start();
        }
    }

    private void startLearner() {
        learnerThread = new Learner(serverId, this);
        learnerThread.start();
    }

    private void startLeaderElection() {
        leaderElectionThread = new LeaderElection(serverId, this);
        leaderElectionThread.start();
    }

    /**
     * shut down method
     */
    public void shutdown() {
        alive.set(false);
        // stop each thread if it exists
        if (acceptorThread != null) {
            acceptorThread.shutdownAcceptor();
        }
        if (proposerThread != null) {
            proposerThread.shutdownProposer();
        }
        if (learnerThread != null) {
            learnerThread.shutdownLearner();
        }
        if (leaderElectionThread != null) {
            leaderElectionThread.shutdownLeaderElection();
        }
        System.out.println("[Server " + serverId + "] has been shut down.");
    }


    // Paxos queue methods
    public void enqueueMessageForAcceptor(PaxosMessage msg) {
        acceptorQueue.add(msg);
    }
    public PaxosMessage pollAcceptorQueue() {
        return acceptorQueue.poll();
    }

    public void enqueueMessageForProposer(PaxosMessage msg) {
        proposerQueue.add(msg);
    }
    public PaxosMessage pollProposerQueue() {
        return proposerQueue.poll();
    }

    public void enqueueMessageForLearner(PaxosMessage msg) {
        learnerQueue.add(msg);
    }
    public PaxosMessage pollLearnerQueue() {
        return learnerQueue.poll();
    }

    // Paxos broadcast helpers
    public void broadcastToAcceptors(PaxosMessage msg) {
        for (PaxosServer s : allServers) {
            s.enqueueMessageForAcceptor(msg);
        }
    }

    public void broadcastAccepted(PaxosMessage msg) {
        for (PaxosServer s : allServers) {
            s.enqueueMessageForLearner(msg);
        }
    }

    public void sendToProposer(PaxosMessage msg, int proposerServerId) {
        allServers[proposerServerId].enqueueMessageForProposer(msg);
    }

    // Paxos data storage
    public void putPendingValue(int instanceNumber, String value) {
        pendingValues.put(instanceNumber, value);
    }

    public String getPendingValue(int instanceNumber) {
        return pendingValues.get(instanceNumber);
    }


    // getters and setters method
    public int getServerId() {
        return serverId;
    }

    public int getNumServers() {
        return numServers;
    }

    public PaxosServer[] getAllServers() {
        return allServers;
    }

    public void setAllServers(PaxosServer[] all) {
        this.allServers = all;
    }

    // Leader election
    public boolean isLeader() {
        return isLeader;
    }
    public void setLeader(boolean leader) {
        if (leader && !isLeader) {
            System.out.println("[Server " + serverId + "] I AM LEADER now.");
        }
        this.isLeader = leader;
    }

    public boolean isAliveServer() {
        return alive.get();
    }


    // KV-Store
    public synchronized void applyChosenValue(String operation) {
        if (operation == null) return;
        String[] parts = operation.split(":");
        if (parts.length < 2) return;

        String opType = parts[0];
        String key = parts[1];

        Integer val = null;
        if (parts.length > 2) {
            try {
                val = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                // ignore parse errors
            }
        }

        if ("PUT".equalsIgnoreCase(opType)) {
            // put(key, val)
            if (key != null && val != null) {
                store.put(key, val);
            }
        } else if ("DELETE".equalsIgnoreCase(opType)) {
            // remove key entirely
            if (key != null) {
                store.remove(key);
            }
        }
    }

    // Leader approach for put/delete
    public Integer get(String key) {
        return store.get(key);
    }

    public void put(String key, int val) {
        if (isLeader()) {
            proposeOperation("PUT:" + key + ":" + val);
        } else {
            PaxosServer leader = findLeader();
            if (leader != null) {
                leader.put(key, val);
            } else {
                System.out.println("Server " + serverId + " No leader found for PUT.");
            }
        }
    }

    public void delete(String key, Integer val) {
        if (isLeader()) {
            if (val == null) {
                proposeOperation("DELETE:" + key);
            } else {
                proposeOperation("DELETE:" + key + ":" + val);
            }
        } else {
            PaxosServer leader = findLeader();
            if (leader != null) {
                leader.delete(key, val);
            } else {
                System.out.println("Server " + serverId + " No leader found for DELETE.");
            }
        }
    }

    // helper to create a new Paxos instanceNumber, store the operation and ask the Proposer to propose it.
    private void proposeOperation(String op) {
        int instanceNumber = new Random().nextInt(1_000_000_00);
        putPendingValue(instanceNumber, op);

        if (proposerThread != null && proposerThread.isAlive()) {
            proposerThread.proposeValue(op, instanceNumber);
        } else {
            System.out.println("Server " + serverId + " Proposer not alive or wrong type. Can't propose.");
        }
    }

    /**
     * Finds a server that is recognized as leader, or null if none found.
     */
    private PaxosServer findLeader() {
        if (allServers == null) return null;
        for (PaxosServer s : allServers) {
            if (s.isLeader()) {
                return s;
            }
        }
        return null;
    }
}
