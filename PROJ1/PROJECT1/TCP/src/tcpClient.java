import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class tcpClient {
    private static final int timeOut = 7000;

    public static void main(String[] args) {
        // validate user input format
        if (args.length != 2) {
            System.out.println("Usage: java client <server_ip> <port_number>");
            return;
        }

        String serverAddress = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try {
             // resolve the hostname
             InetAddress inetAddress = InetAddress.getByName(serverAddress);
             System.out.println("Connected to server: " + inetAddress + " on port " + portNumber);

             Socket socket = new Socket(inetAddress, portNumber);

             // set the timeout
             socket.setSoTimeout(timeOut);

             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());


            // pre-populate the key-value store
            Map<String, Integer> populatedData = new HashMap<>();
            populatedData.put("MacBook", 200);
            populatedData.put("AppleWatch", 500);
            populatedData.put("iPad", 300);
            populatedData.put("VisionPro", 50);
            populatedData.put("HomePod", 100);

            // execute five put operations
            put(populatedData, input, output);
            // execute five get operations
            get(populatedData, input, output);
            // execute five delete operations
            delete(populatedData, input, output);


            Scanner scanner = new Scanner(System.in);
            System.out.println("Which operation do you want to execute? PUT/GET/DELETE. If not, please type EXIT to quit.");

            while (true){
                String userInput = scanner.nextLine();

                // exit the program
                if (userInput.equals("EXIT")){
                    break;
                }

                // send data to server
                output.writeUTF(userInput);
                String response = input.readUTF();
                System.out.println(response);
            }

            scanner.close();
            output.close();
            input.close();
            socket.close();

        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }

    }

    /**
     *
     * @param data: key-value pair in the map
     * @param input: data from client
     * @param output: date to server
     */
    private static void put(Map<String, Integer> data, DataInputStream input, DataOutputStream output) throws IOException{

        for (Map.Entry<String, Integer> entry: data.entrySet()){
            String request = "PUT " + entry.getKey() + " " + entry.getValue();
            System.out.println(request);
            requestWithTimeOut(request, input, output);
        }
    }

    /**
     *
     * @param data: key-value pair in the map
     * @param input: data from client
     * @param output: date to server
     */
    private static void get(Map<String, Integer> data, DataInputStream input, DataOutputStream output) throws IOException{

        for (String key: data.keySet()){
            String request = "GET " + key;
            System.out.println(request);
            requestWithTimeOut(request, input, output);
        }
    }

    /**
     *
     * @param data: key-value pair in the map
     * @param input: data from client
     * @param output: date to server
     */
    private static void delete(Map<String, Integer> data, DataInputStream input, DataOutputStream output) throws IOException{

        for (String key: data.keySet()){
            String request = "DELETE " + key;
            System.out.println(request);
            requestWithTimeOut(request, input, output);
        }
    }

    /**
     *
     * @param request operations of client
     * @param input data from client
     * @param output data to server
     */
    private static void requestWithTimeOut(String request, DataInputStream input, DataOutputStream output){
        try {
            // send request to server
            output.writeUTF(request);

            // wait for the response of server
            String responseFromServer = input.readUTF();
            System.out.println("Response from server: " + responseFromServer);}
        catch (SocketTimeoutException e){
            System.out.println("Timeout Error. The server doesn't respond to " + request);
        }
        catch (IOException e){
            System.out.println("IO Error: " + e.getMessage());
        }
    }
}
