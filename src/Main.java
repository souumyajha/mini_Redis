import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    public static void main(String[] args) {

        // Shared storage for all clients
        ConcurrentHashMap<String, String> data =
                new ConcurrentHashMap<>();

        try {
            // Create server and listen on port 6379
            ServerSocket serverSocket = new ServerSocket(6379);

            System.out.println("Redis server has started on port 6379");

            // Keep accepting new clients
            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("Client connected...");

                // Create a separate thread for this client
                Thread clientThread = new Thread(() -> {

                    try {

                        InputStream input =
                                clientSocket.getInputStream();

                        OutputStream output =
                                clientSocket.getOutputStream();

                        // Keep handling commands from this client
                        while (true) {

                            // Receive and parse RESP command
                            String[] parts =
                                    CommandParser.parse(input);

                            System.out.println("Received command:");

                            for (String part : parts) {
                                System.out.println(part);
                            }

                            // -------------------------
                            // SET command
                            // -------------------------
                            if (parts[0].equals("SET")) {

                                String key = parts[1];
                                String value = parts[2];

                                data.put(key, value);

                                output.write("+OK\r\n".getBytes());
                                output.flush();
                            }

                            // -------------------------
                            // GET command
                            // -------------------------
                            else if (parts[0].equals("GET")) {

                                String key = parts[1];

                                String value = data.get(key);

                                if (value != null) {

                                    output.write(
                                            ("$"+ value.length()+"\r\n" + value +"\r\n").getBytes()
                                    );

                                } else {

                                    output.write(
                                            "$-1\r\n".getBytes()
                                    );
                                }

                                output.flush();
                            }

                            // -------------------------
                            // DEL command
                            // -------------------------
                            else if (parts[0].equals("DEL")) {

                                String key = parts[1];

                                String removeValue =
                                        data.remove(key);

                                if (removeValue != null) {
                                    output.write(":1\r\n".getBytes());
                                } else {
                                    output.write(":0\r\n".getBytes());
                                }

                                output.flush();
                            }

                            // -------------------------
                            // EXISTS command
                            // -------------------------
                            else if (parts[0].equals("EXISTS")) {

                                String key = parts[1];

                                if (data.containsKey(key)) {
                                    output.write(":1\r\n".getBytes());
                                } else {
                                    output.write(":0\r\n".getBytes());
                                }

                                output.flush();
                            }
                        }

                    } catch (IOException e) {

                        System.out.println(
                                "Client connection closed."
                        );
                    }

                });

                // Start the thread
                clientThread.start();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}