
# README

## 1. Build Instructions
- Compile each java file with:
  ```
  javac filename.java
  ```


## 2. Run Instructions
- First, start the server. In the terminal, run:
  ```
  java server
  ```
  After the server starts, it will display “AppleStore server is running.” and wait for an "ENTER" to stop the server.
- Open another terminal and run the client:
  ```
  java client
  ```
  Then the client will connect to the server and automatically executes the PUT, GET, and DELETE operations one by one.

## 3. Executive Summary

### Assignment Overview
The purpose of this assignment, I think, is to implement a key-value store using Java RMI with multithreading support. The server must handle concurrent requests like PUT, GET, and DELETE operations from multiple clients, while managing mutual exclusion. This project's scope mainly involves implementing RPC through Java RMI, and verifying data consistency when there are multiple clients are doing same or different operations at the same time.

### Technical Impression
Through this assignment, I gained practical experience in multithreading and concurrent programming. Using ExecutorService to manage thread pools and ConcurrentHashMap to process data can efficiently respond to multiple client requests. But when implementing the DELETE operation, I realized that I needed to be extra cautious because it involves multiple steps and data consistency must be guaranteed through appropriate operations. Although it is difficult to debug concurrency issues, it also gave me a deeper understanding of concurrency control and data consistency.

This project gave me a more comprehensive understanding of distributed systems and concurrent programming, and made me realize that thread safety issues need to be considered at the beginning, rather than waiting until problems arise.