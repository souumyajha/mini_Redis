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

                            byte[] buffer = new byte[1024];

                            int bytesRead = input.read(buffer);

                            // Client disconnected
                            if (bytesRead == -1) {
                                System.out.println(
                                        "Client disconnected."
                                );
                                break;
                            }

                            // Convert bytes into String
                            String message =
                                    new String(buffer, 0, bytesRead);

                            System.out.println(
                                    "Received: " + message
                            );

                            // Split command into parts
                            String[] parts = message.split(" ");

                            // -------------------------
                            // SET command
                            // -------------------------
                            if (parts[0].equals("SET")) {

                                String key = parts[1];
                                String value = parts[2];

                                data.put(key, value);

                                output.write("OK".getBytes());
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
                                            value.getBytes()
                                    );

                                } else {

                                    output.write(
                                            "NULL".getBytes()
                                    );
                                }

                                output.flush();
                            }
                            else if(parts[0].equals("DEL")){
                                String key = parts[1];

                                String removeValue = data.remove(key);

                                if(removeValue != null){
                                    output.write("1".getBytes());
                                }else{
                                    output.write("0".getBytes());
                                }
                                output.flush();
                            }else if (parts[0].equals("EXISTS")) {

                                String key = parts[1];

                                if (data.containsKey(key)) {
                                    output.write("1".getBytes());
                                } else {
                                    output.write("0".getBytes());
                                }

                                output.flush();
                            }
                        }

                        clientSocket.close();

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