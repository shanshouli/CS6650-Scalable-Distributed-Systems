
## **README**

### **How to Run the Program**

1. **Compile all Java files**  
   Make sure you are in the `src/` folder. Then run:
   ```bash
   javac *.java
   ```

2. **Run the main program**  
   The entry point is the `Client` class. Run this:
   ```bash
   java Client
   ```

3. **Shutdown**  
   After all operations, the program will show the results and  stop all threads and print:
   ```
   All servers shut down.
   ```




---

### **Assignment Overview**

In this assignment, I built a fault-tolerant key-value store using the Paxos protocol. The system consists of multiple servers, and each server runs four main components: Proposer, Acceptor, Learner, and LeaderElection. The goal is to make sure that all replicas agree on updates even when some servers fail. Compared to the previous project which used Two-Phase Commit (2PC), this one focuses more on achieving agreement in a fault-tolerant way. The leader is selected based on the highest alive server ID, and only the leader can propose values. Clients can do `PUT`, `GET`, and `DELETE` operations, and Paxos ensures that all `PUT` and `DELETE` are applied consistently across servers. The assignment helps me understand how consensus algorithms work and how they are used in distributed systems.

---

### **Technical Impression**

At the beginning, it was a bit hard to fully understand how Paxos works, especially the message flows between Proposer, Acceptor, and Learner. But once I broke it into phases and followed how each thread handles messages, it became more clear. I found it helpful to use print statements to debug and see the sequence of messages. One tricky part was to make sure each thread gets the correct message from its queue, and sometimes messages would be missed if the queue was not polled properly. Another challenge was making sure that only the leader proposes new values, and the others don't interfere.

I also ran into a NullPointerException at first, which turned out to be because I passed a null value into the internal map. Fixing that helped me learn how careful we need to be when dealing with multithreaded code and shared state. In the end, when all threads started correctly and the logs showed values being proposed, accepted, and learned, it felt great to see the system working. I think this project gave me a better understanding of real-world problems like consistency and fault tolerance, and how Paxos tries to solve them. I also learned how important good logging and debugging are when working with distributed systems. It's hard but meaningful!
