import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

public class udpServer {

    private static final HashMap<String, Integer> inventory = new HashMap<>();
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        // Check port number
        if (args.length != 1) {
            System.out.println("Usage: java UDPServer <port_number>");
            return;
        }

        // get the port and record in log file
        int port = Integer.parseInt(args[0]);
        clientLog.log("UDP Server is running on port " + port);

        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                // Receive a packet
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    socket.receive(packet);
                }
                catch (IOException e) {
                    clientLog.log("Error in receiving packet: " + e.getMessage());
                    continue;
                }

                // Malformed request
                String request = new String(packet.getData(), 0, packet.getLength()).trim();
                if (request.isEmpty()) {
                    clientLog.log("Malformed request of length " + packet.getLength() + " from " + packet.getAddress() + ": " + packet.getPort());
                    continue;
                }

                clientLog.log("Received from " + packet.getAddress() + ": " + packet.getPort() + request);

                // process the request
                String reply = handleRequest(request);
                clientLog.log("Reply to " + packet.getAddress() + ":" + packet.getPort() + reply);

                // send the reply to the client
                byte[] replyBytes = reply.getBytes();
                DatagramPacket replyPacket = new DatagramPacket(replyBytes, replyBytes.length, packet.getAddress(), packet.getPort());

                try {
                    socket.send(replyPacket);
                }
                catch (IOException e) {
                    clientLog.log("Error of sending reply: " + e.getMessage());
                }
            }

        } catch (SocketException e) {
            System.out.println("Error of socket: " + e.getMessage());
        }
    }

    /**
     *
     * @param req request from client
     * @return handled request
     */
    private static String handleRequest(String req) {
        try (Scanner scanner = new Scanner(req)) {

            // check the legal number of request
            if (!scanner.hasNext()) {
                return "Invalid request!";
            }
            String command = scanner.next();

            if (!scanner.hasNext()) {
                return "Key is required.";
            }
            String key = scanner.next();

            Integer value = scanner.hasNext() ? Integer.valueOf(scanner.next().trim()) : null;

            // operation execution
            if (command.equals("PUT")) {
                if (value == null) {
                    return "Parameter of value is missing.";
                }

                inventory.put(key, value);
                return "PUT " + key + ": " + value;

            }

            else if (command.equals("GET")) {
                if (!inventory.containsKey(key)) {
                    return "GET " + key + ": does not exist.";
                }
                return "GET " + key + ": " + inventory.get(key);
            }

            else if (command.equals("DELETE")) {
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

            }

            else {
                return "Illegal request. Please enter again.";
            }
        }
        catch (Exception e) {
            return "Malformed request received: " + e.getMessage();
        }
    }
}

