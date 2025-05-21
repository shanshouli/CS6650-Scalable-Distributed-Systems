// types of Paxos message between servers
public enum PaxosMessageType {

    // proposer's prepare request (phase 1)
    PREPARE_REQUEST,

    // acceptor's phase 1 response
    PROMISE,

    // proposer's accept request (phase 2)
    ACCEPT_REQUEST,

    // acceptor's phase 2  response
    ACCEPTED,

    // for leader election, assume "highest ID alive is leader."
    ELECTION_PING,

    ELECTION_REPLY
}

