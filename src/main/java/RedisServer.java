//RedisServer → manages the server and accepts clients

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class RedisServer {

    private final int port;

    private final ConcurrentHashMap<String, RedisEntry> data;

    public RedisServer(int port) throws IOException {

        this.port = port;

        data = PersistenceManager.load();

        System.out.println(
                "Loaded " + data.size() +
                        " entries from disk."
        );
    }

    public void start() throws IOException {

        ServerSocket serverSocket =
                new ServerSocket(port, 50, java.net.InetAddress.getByName("0.0.0.0"));

        System.out.println(
                "Redis server has started on port " + port
        );

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

        while (true) {

            Socket clientSocket =
                    serverSocket.accept();

            System.out.println(
                    "Client connected..."
            );

            Thread clientThread =
                    new Thread(
                            new ClientHandler(
                                    clientSocket,
                                    data
                            )
                    );

            clientThread.start();
        }
    }
}
