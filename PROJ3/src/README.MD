# Project 3 Multi-threaded Key-Value Store using RPC

## Overview
This project implements a distributed Key-Value store with multiple replicas, coordinated by a Load Balancer using Two-Phase Commit (2PC) protocol for consistency. It supports `PUT`, `GET`, and `DELETE` operations.

## Components
- **AppleStoreInterface.java**  
  Defines RMI methods for key-value operations (PUT/GET/DELETE).

- **AppleStoreImplementation.java**  
  Implements AppleStoreInterface.

- **LoadBalancerInterface.java**  
  Defines the functions of Load Balancer.

- **LoadBalancerImplementation.java**  
  Implements the 2PC logic for `PUT` and `DELETE`, selects replicas for `GET`, and retrieves all replica states.

- **LoadBalancerServer.java**  
  Launches the RMI registry and registers the Load Balancer object.

- **server.java**  
  Starts a new replica server.

- **client.java**  
  Runs multi-threaded tests.
 


## How to Run
1. Compile all java files by `javac *.java`.
1. Activate Load Balancer through `java LoadBalancerServer.java`.
2. Start building replica via `java server [number]`, e.g. `java server 1`
3. Run `java client.java` to test project.



## Assignment Overview
The task this time is to use Java RMI to build a distributed key-value storage system which consists of several replica servers and a load balancer. The client can directly interact with the load balancer. The system must support operations such as storing data (PUT), retrieving data (GET) and deleting data (DELETE), and must use the two-phase commit protocol (2PC) to ensure data consistency among all replicas. The load balancer is responsible for all write and delete requests, and read requests can be handled by any replica. In addition, thread safety and concurrent request processing must be considered, and finally a client program must be written to demonstrate how the system works. In general, this task gave me a deeper understanding of distributed systems, especially how to ensure consistency and coordination among different servers.



## Technical impression
This project is quite interesting, but also quite challenging. The most troublesome thing is to make the 2PC protocol run between multiple replicas. It is not difficult to write functions such as prepare, commit, and abort, but it is really difficult to debug to ensure that all replicas can respond in real time. There is also the issue of thread safety. I used thread pools and ConcurrentHashMap to handle concurrent requests, and the effect was quite good.

In addition, I found that if the client directly accesses the replica, data consistency is prone to problems, so it is necessary to force all operations to go through the load balancer. There is also a very practical idea, which is to print the status of each replica after each operation, so that I can intuitively see how the data is synchronized.

In general, this task gave me a more deep understanding of distributed systems, especially the practical application of protocols such as 2PC. At the same time, it also made me realize that in actual development, coordination and thread safety are really the top priority, and problems will occur if I am not careful. In the future, I think I can do more on repair inconsistent data if it happens.
