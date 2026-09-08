//PersistenceManager → saves/loads data from disk

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceManager {

    private static final String FILE_NAME = "redis-data.db";

    // Save all data to disk
    public static synchronized void save(
            Map<String, RedisEntry> data) throws IOException {

        try (DataOutputStream output =
                     new DataOutputStream(
                             new FileOutputStream(FILE_NAME))) {

            // Save number of entries
            output.writeInt(data.size());

            for (Map.Entry<String, RedisEntry> entry :
                    data.entrySet()) {

                String key = entry.getKey();
                RedisEntry redisEntry = entry.getValue();

                // Save key
                writeString(output, key);

                // Save value
                writeString(output, redisEntry.getValue());

                // Save expiration timestamp
                output.writeLong(
                        redisEntry.getExpirationTime()
                );
            }
        }
    }

    // Load data from disk
    public static ConcurrentHashMap<String, RedisEntry> load()
            throws IOException {

        ConcurrentHashMap<String, RedisEntry> data =
                new ConcurrentHashMap<>();

        try (DataInputStream input =
                     new DataInputStream(
                             new FileInputStream(FILE_NAME))) {

            int numberOfEntries = input.readInt();

            for (int i = 0; i < numberOfEntries; i++) {

                String key = readString(input);

                String value = readString(input);

                long expirationTime =
                        input.readLong();

                // Don't load entries that expired
                // while the server was offline
                if (expirationTime != -1 &&
                        System.currentTimeMillis()
                                >= expirationTime) {

                    continue;
                }

                RedisEntry entry =
                        new RedisEntry(
                                value,
                                expirationTime
                        );

                data.put(key, entry);
            }

        } catch (EOFException e) {

            System.out.println(
                    "Persistence file is empty or corrupted."
            );
        }

        return data;
    }

    private static void writeString(
            DataOutputStream output,
            String value) throws IOException {

        byte[] bytes =
                value.getBytes(StandardCharsets.UTF_8);

        output.writeInt(bytes.length);

        output.write(bytes);
    }

    private static String readString(
            DataInputStream input) throws IOException {

        int length = input.readInt();

        byte[] bytes =
                new byte[length];

        input.readFully(bytes);

        return new String(
                bytes,
                StandardCharsets.UTF_8
        );
    }
}