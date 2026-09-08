//CommandHandler → decides what SET/GET/etc. actually do

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler {

    private final ConcurrentHashMap<String, RedisEntry> data;

    public CommandHandler(
            ConcurrentHashMap<String, RedisEntry> data) {

        this.data = data;
    }

    public String handle(String[] parts) throws IOException {

        String command = parts[0].toUpperCase();

        // ---------------- SET ----------------
        if (command.equals("SET")) {

            String key = parts[1];
            String value = parts[2];

            RedisEntry entry =
                    new RedisEntry(value);

            // SET key value EX seconds
            if (parts.length == 5 &&
                    parts[3].equalsIgnoreCase("EX")) {

                long seconds =
                        Long.parseLong(parts[4]);

                entry.setExpirationTime(seconds);
            }

            data.put(key, entry);

            PersistenceManager.save(data);

            return "+OK\r\n";
        }

        // ---------------- GET ----------------
        else if (command.equals("GET")) {
            if (parts.length != 2) {
                return "-ERR wrong number of arguments for 'get' command\r\n";
            }

            String key = parts[1];

            RedisEntry entry =
                    data.get(key);

            if (entry == null) {

                return "$-1\r\n";

            } else if (entry.isExpired()) {

                data.remove(key);

                return "$-1\r\n";

            } else {

                String value =
                        entry.getValue();

                byte[] valueBytes =
                        value.getBytes(StandardCharsets.UTF_8);

                return "$" +
                        valueBytes.length +
                        "\r\n" +
                        value +
                        "\r\n";
            }
        }

        // ---------------- DEL ----------------
        else if (command.equals("DEL")) {

            String key = parts[1];

            RedisEntry removeValue =
                    data.remove(key);

            PersistenceManager.save(data);

            if (removeValue != null) {
                return ":1\r\n";
            }

            return ":0\r\n";
        }

        // ---------------- EXISTS ----------------
        else if (command.equals("EXISTS")) {

            String key = parts[1];

            RedisEntry entry =
                    data.get(key);

            if (entry != null &&
                    !entry.isExpired()) {

                return ":1\r\n";

            } else {

                if (entry != null) {
                    data.remove(key);
                }

                return ":0\r\n";
            }
        }

        // ---------------- PING ----------------
        else if (command.equals("PING")) {

            return "+PONG\r\n";
        }

        // ---------------- KEYS ----------------
        else if (command.equals("KEYS")) {

            StringBuilder response =
                    new StringBuilder();

            response.append("*")
                    .append(data.size())
                    .append("\r\n");

            for (String key : data.keySet()) {

                byte[] keyBytes =
                        key.getBytes(StandardCharsets.UTF_8);

                response.append("$")
                        .append(keyBytes.length)
                        .append("\r\n");

                response.append(key)
                        .append("\r\n");
            }

            return response.toString();
        }

        // ---------------- FLUSHALL ----------------
        else if (command.equals("FLUSHALL")) {

            data.clear();

            PersistenceManager.save(data);

            return "+OK\r\n";
        }

        // ---------------- EXPIRE ----------------
        else if (command.equals("EXPIRE")) {

            String key = parts[1];

            long seconds =
                    Long.parseLong(parts[2]);

            RedisEntry entry =
                    data.get(key);

            if (entry == null ||
                    entry.isExpired()) {

                return ":0\r\n";

            } else {

                entry.setExpirationTime(seconds);

                PersistenceManager.save(data);

                return ":1\r\n";
            }
        }

        // ---------------- TTL ----------------
        else if (command.equals("TTL")) {

            String key = parts[1];

            RedisEntry entry =
                    data.get(key);

            if (entry == null) {

                return ":-2\r\n";

            } else if (entry.isExpired()) {

                data.remove(key);

                return ":-2\r\n";

            } else {

                long remaining =
                        entry.getRemainingSeconds();

                return ":" +
                        remaining +
                        "\r\n";
            }
        }

        // Unknown command
        return "-ERR unknown command\r\n";
    }
}