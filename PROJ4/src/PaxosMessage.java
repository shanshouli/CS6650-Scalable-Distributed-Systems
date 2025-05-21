import java.io.Serializable;

public class PaxosMessage implements Serializable {
    private PaxosMessageType type;

    // proposal id in Paxos
    private int proposalNumber;

    // the value being proposed
    private String value;

    // the id of the server that sent the message
    private int senderId;

    // identify which round in the multi-instance Paxos
    private int instanceNumber;

    // label the previously accepted proposals
    private int highestAcceptedProposalNumber;
    private String highestAcceptedValue;

    public PaxosMessage(PaxosMessageType type, int proposalNumber, String value,
                        int senderId, int instanceNumber) {
        this.type = type;
        this.proposalNumber = proposalNumber;
        this.value = value;
        this.senderId = senderId;
        this.instanceNumber = instanceNumber;
    }

    /**
     * get the type of Paxos message
     * @return
     */
    public PaxosMessageType getType() {
        return type;
    }

    /**
     * get the number of proposal
     * @return
     */
    public int getProposalNumber() {
        return proposalNumber;
    }

    /**
     * get the value being proposed
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * get the id of sender
     * @return
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * get the instance number
     * @return
     */
    public int getInstanceNumber() {
        return instanceNumber;
    }

    /**
     * get the highest accepted proposal number of acceptor
     * @return
     */
    public int getHighestAcceptedProposalNumber() {
        return highestAcceptedProposalNumber;
    }

    /**
     * get the highest accepted value of acceptor
     * @return
     */
    public String getHighestAcceptedValue() {
        return highestAcceptedValue;
    }

    /**
     * set the highest accepted proposal number of acceptor
     * @return
     */
    public void setHighestAcceptedProposalNumber(int n) {
        this.highestAcceptedProposalNumber = n;
    }

    /**
     * set the highest accepted value of acceptor
     * @param v
     */
    public void setHighestAcceptedValue(String v) {
        this.highestAcceptedValue = v;
    }
}
