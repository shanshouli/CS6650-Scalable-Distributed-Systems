import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class udpClient {

    private static final int timeOut = 7000;
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {

        // Check server ip and port number
        if (args.length != 2) {
            System.out.println("Usage: java udpClient <server_ip> <port_number>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            // connect client to server
            InetAddress serverAddress = InetAddress.getByName(ip);
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeOut);
            System.out.println("Connect to server: " + serverAddress + " on port " + port);

            // Pre-populated data.
            Map<String, Integer> populatedData = new HashMap<>();
            populatedData.put("MacBook", 200);
            populatedData.put("AppleWatch", 500);
            populatedData.put("iPad", 300);
            populatedData.put("VisionPro", 50);
            populatedData.put("HomePod", 100);

            // PUT operations
            for (Map.Entry<String, Integer> entry: populatedData.entrySet()) {
                String req = "PUT " + entry.getKey() + " " + entry.getValue();
                System.out.println("Sending: " + req);
                sendRequest(req, socket, serverAddress, port);
            }

            // GET operations
            for (String key: populatedData.keySet()) {
                String req = "GET " + key;
                System.out.println("Sending: " + req);
                sendRequest(req, socket, serverAddress, port);
            }

            // DELETE operations
            for (String key: populatedData.keySet()) {
                String req = "DELETE " + key + " " + 50;
                System.out.println("Sending: " + req);
                sendRequest(req, socket, serverAddress, port);
            }


            Scanner scanner = new Scanner(System.in);
            System.out.println("Which operation do you want to execute? PUT/GET/DELETE. If not, please type EXIT to quit.");

            while (true) {
                String userInput = scanner.nextLine();
                if (userInput.equals("EXIT")) {
                    break;
                }
                sendRequest(userInput, socket, serverAddress, port);
            }

            scanner.close();
            socket.close();
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }

    // send a UDP request and waits for a reply
    private static void sendRequest(String request, DatagramSocket socket, InetAddress address, int port) {
        try {
            byte[] requestBytes = request.getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, address, port);
            socket.send(requestPacket);

            // use a buffer to receive the reply
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            // make the bytes into formatted string
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength()).trim();
            System.out.println("Reply from server: " + response);
        }
        catch (SocketTimeoutException e) {
            System.out.println("Timeout: " + request);
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }
}
