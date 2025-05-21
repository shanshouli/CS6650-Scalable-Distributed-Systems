import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class tcpServer {

    private static final HashMap<String, Integer> inventory = new HashMap<>();

    public static void main(String[] args) {
        // Check port number
        if (args.length != 1) {
            System.out.println("Usage: java tcpServer <port_number>");
            return;
        }
        int portNumber = Integer.parseInt(args[0]);

        System.out.println("Server is listening on port: " + portNumber);

        // Create a ServerSocket and wait for client connections.
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {

                Socket clientSocket = serverSocket.accept();
                clientLog.log("Client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                try (
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                    // Continuously read requests from the client
                    while (true) {
                        String request = in.readUTF();

                        // Process the request
                        String response = requestHandling(request);
                        clientLog.log("Response to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + response);

                        // Send the response to the client
                        out.writeUTF(response);
                    }

                } catch (IOException e) {
                    clientLog.log("Error handling client: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }

    }

    /**
     *
     * @param requestFromClient: request from client
     * @return request after operations
     */
    private static String requestHandling(String requestFromClient) {
        try (Scanner scanner = new Scanner(requestFromClient)) {

            // check the legal number of request
            if (!scanner.hasNext()) {
                return "Invalid request.";
            }
            String request = scanner.next();

            if (!scanner.hasNext()) {
                return "Key is required.";
            }
            String key = scanner.next();

            Integer value = scanner.hasNext() ? Integer.valueOf(scanner.next().trim()) : null;

            // operation execution
            if (request.equals("PUT")) {
                if (value == null) {
                    return "Parameter of value is missing.";
                }
                inventory.put(key, value);
                return "PUT " + key + ": " + value;
            }

            else if (request.equals("GET")) {
                if (!inventory.containsKey(key)) {
                    return "GET " + key + ": does not exist.";
                }
                return "GET " + key + ": " + inventory.get(key);
            }

            else if (request.equals("DELETE")) {
                if (!inventory.containsKey(key)) {
                    return "DELETE " + key + ": does not exist.";
                }
                int currentQuantity = inventory.get(key);
                // If no amount is provided, delete all
                int deleteAmount = (value == null) ? currentQuantity : value;
                int newQuantity = currentQuantity - deleteAmount;

                // Check whether there are still the storages of the item
                if (newQuantity <= 0) {
                    inventory.remove(key);
                    return  "All " + key + " items removed.";
                } else {
                    inventory.put(key, newQuantity);
                    return "DELETE " + key + ": " + deleteAmount + " items removed";
                }
            } else {
                return "Illegal request. Please enter again.";
            }
        }
    }
}
