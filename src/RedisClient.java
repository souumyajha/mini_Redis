import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RedisClient {

    public static void main(String[] args) {

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
            output.write("SET name Soumya".getBytes());
            output.flush();

            bytesRead = input.read(buffer);
            System.out.println(
                    "SET response: " +
                            new String(buffer, 0, bytesRead)
            );

            // EXISTS
            output.write("EXISTS name".getBytes());
            output.flush();

            bytesRead = input.read(buffer);
            System.out.println(
                    "EXISTS response: " +
                            new String(buffer, 0, bytesRead)
            );

           // DEL
            output.write("DEL name".getBytes());
            output.flush();

            bytesRead = input.read(buffer);
            System.out.println(
                    "DEL response: " +
                            new String(buffer, 0, bytesRead)
            );

            // GET after DEL
            output.write("GET name".getBytes());
            output.flush();

            bytesRead = input.read(buffer);
            System.out.println(
                    "GET response: " +
                            new String(buffer, 0, bytesRead)
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
