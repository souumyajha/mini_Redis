//CommandParser → converts RESP bytes into commands

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class CommandParser {

    public static String[] parse(InputStream input) throws IOException {

        // Read the first byte
        int firstByte = input.read();

        // RESP array must start with *
        if (firstByte != '*') {
            throw new IOException("Invalid RESP request");
        }

        // Read number of arguments
        String argumentCountLine = readLine(input);

        int numberOfArguments =
                Integer.parseInt(argumentCountLine);

        String[] arguments =
                new String[numberOfArguments];

        // Read each argument
        for (int i = 0; i < numberOfArguments; i++) {

            // Read $
            int type = input.read();

            if (type != '$') {
                throw new IOException("Expected bulk string");
            }

            // Read length
            String lengthLine = readLine(input);

            int length =
                    Integer.parseInt(lengthLine);

            // Read exactly 'length' bytes
            byte[] data = new byte[length];

            readFully(input, data);

            // Convert bytes to String
            arguments[i] =
                    new String(data, StandardCharsets.UTF_8);

            // Read CRLF after the value
            input.read();
            input.read();
        }

        return arguments;
    }


    private static String readLine(InputStream input)
            throws IOException {

        StringBuilder result = new StringBuilder();

        int currentByte;

        while (true) {

            currentByte = input.read();

            if (currentByte == '\r') {

                int nextByte = input.read();

                if (nextByte == '\n') {
                    break;
                }
            }

            result.append((char) currentByte);
        }

        return result.toString();
    }


    private static void readFully(
            InputStream input,
            byte[] data) throws IOException {

        int totalRead = 0;

        while (totalRead < data.length) {

            int bytesRead =
                    input.read(
                            data,
                            totalRead,
                            data.length - totalRead
                    );

            if (bytesRead == -1) {
                throw new IOException(
                        "Client disconnected"
                );
            }

            totalRead += bytesRead;
        }
    }
}