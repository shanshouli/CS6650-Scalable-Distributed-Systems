import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Proposer also has a random lifetime for failure testing.
 * If it's not the leader, it won't propose, which prevents live lock.
 */
public class Proposer extends Thread {
    private final int serverId;
    private final PaxosServer parentServer;
    private final AtomicBoolean running;
    private final Random random;

    // Unique proposal number generator for this server
    private final AtomicInteger proposalCounter = new AtomicInteger(0);

    // how many PROMISE messages we've received
    private final Map<Integer, Integer> promiseCount = new ConcurrentHashMap<>();

    // the highest accepted proposal from any acceptor
    private final Map<Integer, Integer> highestProposalSeen = new ConcurrentHashMap<>();

    // the value corresponding to that highest accepted proposal
    private final Map<Integer, String> highestValueSeen = new ConcurrentHashMap<>();

    public Proposer(int serverId, PaxosServer parentServer) {
        this.serverId = serverId;
        this.parentServer = parentServer;
        this.running = new AtomicBoolean(true);
        this.random = new Random();
    }

    @Override
    public void run() {
        System.out.println("Proposer " + serverId + " STARTED.");
        // random lifetime (5-10 seconds)
        long lifetimeMillis = 5000 + random.nextInt(5000);
        long startTime = System.currentTimeMillis();

        while (running.get()) {
            if (System.currentTimeMillis() - startTime > lifetimeMillis) {
                System.out.println("Proposer " + serverId + " FAILING...");
                running.set(false);
                break;
            }

            // the Proposer logic runs if we are the leader
            PaxosMessage msg = parentServer.pollProposerQueue();
            if (msg != null) {
                if (msg.getType() == PaxosMessageType.PROMISE) {
                    onPromise(msg);
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                running.set(false);
            }
        }
        System.out.println("Proposer " + serverId + " STOPPED.");
    }

    /**
     * Generates a new proposal number for Phase 1
     */
    private int generateProposalNumber() {
        int base = proposalCounter.incrementAndGet();
        return (base << 8) + serverId;
    }

    /**
     * Propose a value for a given instanceNumber for multi-instance Paxos
     */
    public void proposeValue(String value, int instanceNumber) {

        if (!parentServer.isLeader()) {
            // if not the leader, do nothing
            System.out.println("Proposer " + serverId + " Not leader, can't propose instance " + instanceNumber);
            return;
        }

        int proposalNumber = generateProposalNumber();

        // initialize counts
        promiseCount.put(instanceNumber, 0);
        highestProposalSeen.put(instanceNumber, -1);
        highestValueSeen.put(instanceNumber, value);

        // broadcast PREPARE to acceptors
        PaxosMessage prepareMsg = new PaxosMessage(
                PaxosMessageType.PREPARE_REQUEST,
                proposalNumber,
                null,
                serverId,
                instanceNumber
        );
        parentServer.broadcastToAcceptors(prepareMsg);
    }

    /**
     * Handle PROMISE messages from Acceptors
     */
    private synchronized void onPromise(PaxosMessage msg) {
        int instanceNumber = msg.getInstanceNumber();
        int currentCount = promiseCount.getOrDefault(instanceNumber, 0) + 1;
        promiseCount.put(instanceNumber, currentCount);

        // If the Acceptor has a previously accepted proposal, see if it's the highest we've seen
        int acceptorProposal = msg.getHighestAcceptedProposalNumber();
        if (acceptorProposal > highestProposalSeen.getOrDefault(instanceNumber, -1)) {
            highestProposalSeen.put(instanceNumber, acceptorProposal);
            highestValueSeen.put(instanceNumber, msg.getHighestAcceptedValue());
        }

        // If we have a majority, we can do Phase 2
        if (currentCount > (parentServer.getNumServers() / 2)) {

            // If an Acceptor has a higher accepted value, adopt that value
            String valueToPropose = highestValueSeen.get(instanceNumber);

            if (valueToPropose == null) {
                // if none were accepted, propose our own pending value
                valueToPropose = parentServer.getPendingValue(instanceNumber);
            }

            // use the proposalNumber from the PROMISE message
            int proposalNumber = msg.getProposalNumber();
            PaxosMessage acceptReq = new PaxosMessage(
                    PaxosMessageType.ACCEPT_REQUEST,
                    proposalNumber,
                    valueToPropose,
                    serverId,
                    instanceNumber
            );
            parentServer.broadcastToAcceptors(acceptReq);
        }
    }

    /**
     * Shutdown method to stop the thread
     */
    public void shutdownProposer() {
        running.set(false);
        this.interrupt();
    }
}
