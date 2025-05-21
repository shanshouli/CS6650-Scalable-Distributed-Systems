import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

// it runs as a thread, handling the prepare and accept requests from the proposer
public class Acceptor extends Thread {
    private final int serverId;
    private final PaxosServer parentServer;
    private final AtomicBoolean running;
    private final Random random;

    // Paxos in-memory state
    private int promisedProposalNumber = -1;

    // the proposal it has accepted most recently
    private int acceptedProposalNumber = -1;
    private String acceptedValue = null;

    public Acceptor(int serverId, PaxosServer parentServer) {
        this.serverId = serverId;
        this.parentServer = parentServer;
        this.running = new AtomicBoolean(true);
        this.random = new Random();
    }

    @Override
    public void run() {
        System.out.println("Acceptor " + serverId + " STARTED.");
        long lifetimeMillis = 5000 + random.nextInt(5000); // 5–10s
        long startTime = System.currentTimeMillis();

        while (running.get()) {
            // check whether we go beyond thread lifetime
            if (System.currentTimeMillis() - startTime > lifetimeMillis) {
                System.out.println("Acceptor " + serverId + " FAILING.");
                running.set(false);
                break;
            }

            // get next message from the server's acceptor queue
            PaxosMessage msg = parentServer.pollAcceptorQueue();
            if (msg != null) {
                handleMessage(msg);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                running.set(false);
            }
        }
        System.out.println("Acceptor " + serverId + " STOPPED.");
    }

    /**
     * the logic for prepare request and accept request
     * @param msg
     */
    private void handleMessage(PaxosMessage msg) {
        switch (msg.getType()) {
            case PREPARE_REQUEST:

                // Phase 1: acceptor do not accept proposals smaller the new number
                if (msg.getProposalNumber() > promisedProposalNumber) {

                    // update promisedProposalNumber to the new proposal
                    promisedProposalNumber = msg.getProposalNumber();

                    // respond with a PROMISE
                    PaxosMessage promise = new PaxosMessage(
                            PaxosMessageType.PROMISE,
                            msg.getProposalNumber(),
                            null,
                            serverId,
                            msg.getInstanceNumber()
                    );

                    promise.setHighestAcceptedProposalNumber(acceptedProposalNumber);
                    promise.setHighestAcceptedValue(acceptedValue);

                    // send the PROMISE back to the Proposer
                    parentServer.sendToProposer(promise, msg.getSenderId());
                }
                break;

            case ACCEPT_REQUEST:
                // Phase 2: if the proposalNumber is bigger, we accept it
                if (msg.getProposalNumber() >= promisedProposalNumber) {
                    acceptedProposalNumber = msg.getProposalNumber();
                    acceptedValue = msg.getValue();

                    // we broadcast an ACCEPTED message so that Learners can learn it
                    PaxosMessage acceptedMsg = new PaxosMessage(
                            PaxosMessageType.ACCEPTED,
                            acceptedProposalNumber,
                            acceptedValue,
                            serverId,
                            msg.getInstanceNumber()
                    );
                    parentServer.broadcastAccepted(acceptedMsg);

                }
                break;

            default:
                // just ignore other message types
                break;
        }
    }

    /**
     * Shutdown method to stop this thread
     */
    public void shutdownAcceptor() {
        running.set(false);

        this.interrupt();
    }

}
