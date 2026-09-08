import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RedisEntryTest {

    @Test
    void shouldStoreValue() {

        RedisEntry entry =
                new RedisEntry("Soumya");

        assertEquals(
                "Soumya",
                entry.getValue()
        );
    }

    @Test
    void shouldNotExpireByDefault() {

        RedisEntry entry =
                new RedisEntry("Soumya");

        assertFalse(
                entry.isExpired()
        );

        assertEquals(
                -1,
                entry.getRemainingSeconds()
        );
    }

    @Test
    void shouldExpireAfterGivenTime() throws InterruptedException {

        RedisEntry entry =
                new RedisEntry("Soumya");

        entry.setExpirationTime(1);

        assertFalse(
                entry.isExpired()
        );

        Thread.sleep(1100);

        assertTrue(
                entry.isExpired()
        );
    }

    @Test
    void shouldReturnRemainingTTL() {

        RedisEntry entry =
                new RedisEntry("Soumya");

        entry.setExpirationTime(10);

        long ttl =
                entry.getRemainingSeconds();

        assertTrue(
                ttl >= 9 && ttl <= 10
        );
    }
}