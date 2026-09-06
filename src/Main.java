import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static ConcurrentHashMap<String, RedisEntry> loadData() {

        try {

            ConcurrentHashMap<String, RedisEntry> data =
                    PersistenceManager.load();

            System.out.println(
                    "Loaded " + data.size() +
                            " entries from disk."
            );

            return data;

        } catch (IOException e) {

            System.out.println(
                    "Could not load persisted data."
            );

            return new ConcurrentHashMap<>();
        }
    }

    public static void main(String[] args) {

        // Shared storage for all clients
        final ConcurrentHashMap<String, RedisEntry> data =
                loadData();
        // Save data when server shuts down
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {

                    try {

                        PersistenceManager.save(data);

                        System.out.println(
                                "Data saved before shutdown."
                        );

                    } catch (IOException e) {

                        System.out.println(
                                "Could not save data."
                        );
                    }
                })
        );

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

                                RedisEntry entry =
                                        new RedisEntry(value);

                                // SET key value EX seconds
                                if (parts.length == 5 &&
                                        parts[3].equals("EX")) {

                                    long seconds =
                                            Long.parseLong(parts[4]);

                                    entry.setExpirationTime(seconds);
                                }

                                data.put(key, entry);
                                PersistenceManager.save(data);

                                output.write("+OK\r\n".getBytes());
                                output.flush();
                            }

                            // -------------------------
                            // GET command
                            // -------------------------
                            else if (parts[0].equals("GET")) {

                                String key = parts[1];

                                RedisEntry entry =
                                        data.get(key);

                                if (entry == null) {

                                    output.write("$-1\r\n".getBytes());

                                } else if (entry.isExpired()) {

                                    data.remove(key);

                                    output.write("$-1\r\n".getBytes());

                                } else {

                                    String value =
                                            entry.getValue();

                                    output.write(
                                            ("$" + value.length() + "\r\n" +
                                                    value + "\r\n").getBytes()
                                    );
                                }

                                output.flush();
                            }

                            // -------------------------
                            // DEL command
                            // -------------------------
                            else if (parts[0].equals("DEL")) {

                                String key = parts[1];

                                RedisEntry removeValue =
                                        data.remove(key);
                                PersistenceManager.save(data);

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

                                RedisEntry entry =
                                        data.get(key);

                                if (entry != null &&
                                        !entry.isExpired()) {

                                    output.write(":1\r\n".getBytes());

                                } else {

                                    if (entry != null) {
                                        data.remove(key);
                                    }

                                    output.write(":0\r\n".getBytes());
                                }

                                output.flush();
                            }
                            else if (parts[0].equals("EXPIRE")) {

                                String key = parts[1];

                                long seconds =
                                        Long.parseLong(parts[2]);

                                RedisEntry entry =
                                        data.get(key);

                                if (entry == null ||
                                        entry.isExpired()) {

                                    output.write(":0\r\n".getBytes());

                                } else {

                                    entry.setExpirationTime(seconds);
                                    PersistenceManager.save(data);

                                    output.write(":1\r\n".getBytes());
                                }

                                output.flush();
                            }
                            else if (parts[0].equals("TTL")) {

                                String key = parts[1];

                                RedisEntry entry =
                                        data.get(key);

                                if (entry == null) {

                                    output.write(":-2\r\n".getBytes());

                                } else if (entry.isExpired()) {

                                    data.remove(key);

                                    output.write(":-2\r\n".getBytes());

                                } else {

                                    long remaining =
                                            entry.getRemainingSeconds();

                                    output.write(
                                            (":" + remaining + "\r\n").getBytes()
                                    );
                                }

                                output.flush();
                            }
                            else if (parts[0].equals("PING")) {

                                output.write("+PONG\r\n".getBytes());
                                output.flush();
                            }
                            else if (parts[0].equals("KEYS")) {

                                StringBuilder response = new StringBuilder();

                                response.append("*")
                                        .append(data.size())
                                        .append("\r\n");

                                for (String key : data.keySet()) {

                                    response.append("$")
                                            .append(key.length())
                                            .append("\r\n");

                                    response.append(key)
                                            .append("\r\n");
                                }

                                output.write(response.toString().getBytes());
                                output.flush();
                            }else if (parts[0].equals("FLUSHALL")) {

                                data.clear();
                                PersistenceManager.save(data);

                                output.write("+OK\r\n".getBytes());
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