import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceManagerTest {

    @Test
    void shouldSaveAndLoadData() throws Exception {

        ConcurrentHashMap<String, RedisEntry> data =
                new ConcurrentHashMap<>();

        data.put(
                "name",
                new RedisEntry("Soumya")
        );

        PersistenceManager.save(data);

        ConcurrentHashMap<String, RedisEntry> loaded =
                PersistenceManager.load();

        assertTrue(
                loaded.containsKey("name")
        );

        assertEquals(
                "Soumya",
                loaded.get("name").getValue()
        );
    }

    @Test
    void shouldPreserveExpirationTime()
            throws Exception {

        ConcurrentHashMap<String, RedisEntry> data =
                new ConcurrentHashMap<>();

        RedisEntry entry =
                new RedisEntry("Soumya");

        entry.setExpirationTime(60);

        data.put(
                "name",
                entry
        );

        PersistenceManager.save(data);

        ConcurrentHashMap<String, RedisEntry> loaded =
                PersistenceManager.load();

        assertTrue(
                loaded.containsKey("name")
        );

        assertTrue(
                loaded.get("name")
                        .getExpirationTime() > 0
        );
    }
}