public class RedisEntry {

    private String value;
    private long expirationTime;

    // Normal entry without expiration
    public RedisEntry(String value) {
        this.value = value;
        this.expirationTime = -1;
    }

    // Used when loading data from disk
    public RedisEntry(String value, long expirationTime) {
        this.value = value;
        this.expirationTime = expirationTime;
    }

    public String getValue() {
        return value;
    }

    public void setExpirationTime(long seconds) {
        this.expirationTime =
                System.currentTimeMillis() + (seconds * 1000);
    }

    public boolean isExpired() {

        if (expirationTime == -1) {
            return false;
        }

        return System.currentTimeMillis() >= expirationTime;
    }

    public long getRemainingSeconds() {

        if (expirationTime == -1) {
            return -1;
        }

        long remaining =
                expirationTime - System.currentTimeMillis();

        if (remaining <= 0) {
            return -2;
        }

        return remaining / 1000;
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}