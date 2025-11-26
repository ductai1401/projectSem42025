package projectSem4.com.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {

    private static class Entry {
        String code;
        long expiresAt;
        int attempts;
        boolean used;
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final Duration ttl = Duration.ofMinutes(10);
    private final int maxAttempts = 5;

    public String generate(String email) {
        String code = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
        Entry e = new Entry();
        e.code = code;
        e.expiresAt = System.currentTimeMillis() + ttl.toMillis();
        store.put(email.toLowerCase(), e);
        return code;
    }

    public boolean verify(String email, String code) {
        Entry e = store.get(email.toLowerCase());
        if (e == null || e.used) return false;
        if (System.currentTimeMillis() > e.expiresAt) return false;
        if (e.attempts >= maxAttempts) return false;
        e.attempts++;
        if (!Objects.equals(e.code, code)) return false;
        e.used = true; // chống reuse
        return true;
    }

    public void invalidate(String email) {
        store.remove(email.toLowerCase());
    }
}
