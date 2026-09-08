import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RedisClient {

    public static void main(String[] args) throws InterruptedException {

        try {
           // creating a connection with the local host on port no 6379
            Socket socket = new Socket("localhost", 6379);

            System.out.println("Connected to server!");

            //send data through connection
            OutputStream output = socket.getOutputStream();

            System.out.println("Request sent!");
            //to revieve the response from the server
            InputStream input = socket.getInputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            // SET
            output.write(
                    ("*3\r\n" +
                    "$3\r\n" +
                    "SET\r\n" +
                    "$4\r\n" +
                    "name\r\n" +
                    "$6\r\n" +
                    "Soumya\r\n").getBytes()
            );
            output.flush();

            bytesRead = input.read(buffer);
            System.out.println(
                    "SET response: " +
                            new String(buffer, 0, bytesRead)
            );

            // EXISTS
            output.write((
                    "*2\r\n" +
                    "$6\r\n" +
                    "EXISTS\r\n" +
                    "$4\r\n" +
                    "name\r\n").getBytes()
            );
            output.flush();

            bytesRead = input.read(buffer);
            System.out.println(
                    "EXISTS response: " +
                            new String(buffer, 0, bytesRead)
            );

           // DEL
            output.write((
                    "*2\r\n" +
                    "$3\r\n" +
                    "DEL\r\n" +
                    "$4\r\n" +
                    "name\r\n").getBytes());
            output.flush();

            bytesRead = input.read(buffer);
            System.out.println(
                    "DEL response: " +
                            new String(buffer, 0, bytesRead)
            );

            // GET after DEL
            output.write(
                    ("*2\r\n" +
                    "$3\r\n" +
                    "GET\r\n" +
                    "$4\r\n" +
                    "name\r\n").getBytes());
            output.flush();

            bytesRead = input.read(buffer);
            System.out.println(
                    "GET response: " +
                            new String(buffer, 0, bytesRead)
            );
            //PING
            output.write(
                    ("*1\r\n" +
                            "$4\r\n" +
                            "PING\r\n").getBytes()
            );

            output.flush();

            bytesRead = input.read(buffer);

            System.out.println(
                    "PING response: " +
                            new String(buffer, 0, bytesRead)
            );
            //Keys
            output.write(
                    ("*3\r\n" +
                            "$3\r\n" +
                            "SET\r\n" +
                            "$4\r\n" +
                            "name\r\n" +
                            "$6\r\n" +
                            "Soumya\r\n").getBytes()
            );

            output.flush();

            input.read(buffer);

            output.write(
                    ("*1\r\n" +
                            "$4\r\n" +
                            "KEYS\r\n").getBytes()
            );

            output.flush();

            bytesRead = input.read(buffer);

            System.out.println(
                    "KEYS response: " +
                            new String(buffer, 0, bytesRead)
            );

            //flushal
            output.write(
                    ("*1\r\n" +
                            "$8\r\n" +
                            "FLUSHALL\r\n").getBytes()
            );

            output.flush();

            bytesRead = input.read(buffer);

            System.out.println(
                    "FLUSHALL response: " +
                            new String(buffer, 0, bytesRead)
            );// TTL TEST

// SET name Soumya EX 10
            output.write(
                    ("*5\r\n" +
                            "$3\r\n" +
                            "SET\r\n" +
                            "$4\r\n" +
                            "name\r\n" +
                            "$6\r\n" +
                            "Soumya\r\n" +
                            "$2\r\n" +
                            "EX\r\n" +
                            "$2\r\n" +
                            "10\r\n").getBytes()
            );

            output.flush();

            bytesRead = input.read(buffer);

            System.out.println(
                    "SET with TTL response: " +
                            new String(buffer, 0, bytesRead)
            );// TTL name

            output.write(
                    ("*2\r\n" +
                            "$3\r\n" +
                            "TTL\r\n" +
                            "$4\r\n" +
                            "name\r\n").getBytes()
            );

            output.flush();

            bytesRead = input.read(buffer);

            System.out.println(
                    "TTL response: " +
                            new String(buffer, 0, bytesRead)
            );// GET name

            output.write(
                    ("*2\r\n" +
                            "$3\r\n" +
                            "GET\r\n" +
                            "$4\r\n" +
                            "name\r\n").getBytes()
            );

            output.flush();

            bytesRead = input.read(buffer);

            System.out.println(
                    "GET response: " +
                            new String(buffer, 0, bytesRead)
            );

            Thread.sleep(11000);

 //GET after expiration

            output.write(
                    ("*2\r\n" +
                            "$3\r\n" +
                            "GET\r\n" +
                            "$4\r\n" +
                            "name\r\n").getBytes()
            );

            output.flush();

            bytesRead = input.read(buffer);

            System.out.println(
                    "GET after expiration: " +
                            new String(buffer, 0, bytesRead)
            );
            // TTL after expiration

            output.write(
                    ("*2\r\n" +
                            "$3\r\n" +
                            "TTL\r\n" +
                            "$4\r\n" +
                            "name\r\n").getBytes()
            );

            output.flush();

            bytesRead = input.read(buffer);

            System.out.println(
                    "TTL after expiration: " +
                            new String(buffer, 0, bytesRead)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
