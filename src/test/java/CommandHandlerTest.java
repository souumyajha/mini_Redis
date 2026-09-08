import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class CommandHandlerTest {

    private ConcurrentHashMap<String, RedisEntry> data;
    private CommandHandler handler;

    @BeforeEach
    void setUp() {

        data =
                new ConcurrentHashMap<>();

        handler =
                new CommandHandler(data);
    }

    @Test
    void shouldSetValue() throws Exception {

        String response =
                handler.handle(
                        new String[]{
                                "SET",
                                "name",
                                "Soumya"
                        }
                );

        assertEquals(
                "+OK\r\n",
                response
        );

        assertEquals(
                "Soumya",
                data.get("name").getValue()
        );
    }

    @Test
    void shouldGetValue() throws Exception {

        data.put(
                "name",
                new RedisEntry("Soumya")
        );

        String response =
                handler.handle(
                        new String[]{
                                "GET",
                                "name"
                        }
                );

        assertEquals(
                "$6\r\nSoumya\r\n",
                response
        );
    }

    @Test
    void shouldReturnNullForMissingKey() throws Exception {

        String response =
                handler.handle(
                        new String[]{
                                "GET",
                                "missing"
                        }
                );

        assertEquals(
                "$-1\r\n",
                response
        );
    }

    @Test
    void shouldDeleteValue() throws Exception {

        data.put(
                "name",
                new RedisEntry("Soumya")
        );

        String response =
                handler.handle(
                        new String[]{
                                "DEL",
                                "name"
                        }
                );

        assertEquals(
                ":1\r\n",
                response
        );

        assertFalse(
                data.containsKey("name")
        );
    }

    @Test
    void shouldCheckIfKeyExists() throws Exception {

        data.put(
                "name",
                new RedisEntry("Soumya")
        );

        String response =
                handler.handle(
                        new String[]{
                                "EXISTS",
                                "name"
                        }
                );

        assertEquals(
                ":1\r\n",
                response
        );
    }

    @Test
    void shouldReturnPong() throws Exception {

        String response =
                handler.handle(
                        new String[]{
                                "PING"
                        }
                );

        assertEquals(
                "+PONG\r\n",
                response
        );
    }

    @Test
    void shouldFlushAllData() throws Exception {

        data.put(
                "name",
                new RedisEntry("Soumya")
        );

        data.put(
                "city",
                new RedisEntry("Nagpur")
        );

        String response =
                handler.handle(
                        new String[]{
                                "FLUSHALL"
                        }
                );

        assertEquals(
                "+OK\r\n",
                response
        );

        assertTrue(
                data.isEmpty()
        );
    }

    @Test
    void shouldSetExpiration() throws Exception {

        data.put(
                "name",
                new RedisEntry("Soumya")
        );

        String response =
                handler.handle(
                        new String[]{
                                "EXPIRE",
                                "name",
                                "10"
                        }
                );

        assertEquals(
                ":1\r\n",
                response
        );

        assertTrue(
                data.get("name")
                        .getRemainingSeconds() > 0
        );
    }

    @Test
    void shouldReturnTTL() throws Exception {

        data.put(
                "name",
                new RedisEntry("Soumya")
        );

        handler.handle(
                new String[]{
                        "EXPIRE",
                        "name",
                        "10"
                }
        );

        String response =
                handler.handle(
                        new String[]{
                                "TTL",
                                "name"
                        }
                );

        assertTrue(
                response.equals(":9\r\n") ||
                        response.equals(":10\r\n")
        );
    }

    @Test
    void shouldReturnUnknownCommandError()
            throws Exception {

        String response =
                handler.handle(
                        new String[]{
                                "HELLO"
                        }
                );

        assertEquals(
                "-ERR unknown command\r\n",
                response
        );
    }
}
