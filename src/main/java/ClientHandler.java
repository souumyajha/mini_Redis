//ClientHandler → manages one client's connection

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    private final ConcurrentHashMap<String, RedisEntry> data;

    public ClientHandler(
            Socket clientSocket,
            ConcurrentHashMap<String, RedisEntry> data) {

        this.clientSocket = clientSocket;
        this.data = data;
    }

    @Override
    public void run() {

        CommandHandler commandHandler =
                new CommandHandler(data);

        try {

            InputStream input =
                    clientSocket.getInputStream();

            OutputStream output =
                    clientSocket.getOutputStream();

            while (true) {

                String[] parts =
                        CommandParser.parse(input);

                System.out.println(
                        "Received command:"
                );

                for (String part : parts) {
                    System.out.println(part);
                }

                String response =
                        commandHandler.handle(parts);

                output.write(
                        response.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                output.flush();
                if (parts.length > 0 &&
                        parts[0].equalsIgnoreCase("QUIT")) {

                    break;
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Client connection closed."
            );

        } finally {

            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}