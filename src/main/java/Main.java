//Main → starts the application

public class Main {

    public static void main(String[] args) {

        try {

            RedisServer server =
                    new RedisServer(6379);

            server.start();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}