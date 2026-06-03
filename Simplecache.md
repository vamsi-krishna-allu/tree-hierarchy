## Code Review

You are reviewing the following code submitted as part of a task to implement an item cache in a highly concurrent application. The anticipated load includes: thousands of reads per second, hundreds of writes per second, tens of concurrent threads. Your objective is to identify and explain the issues in the implementation that must be addressed before deploying the code to production. Please provide a clear explanation of each issue and its potential impact on production behaviour.

```java
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCache<K, V> {
    private final ConcurrentHashMap<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final long ttlMs = 60000; // 1 minute

    public static class CacheEntry<V> {
        private final V value;
        private final long timestamp;

        public CacheEntry(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public V getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
    }

    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null) {
            if (System.currentTimeMillis() - entry.getTimestamp() < ttlMs) {
                return entry.getValue();
            }
        }
        return null;
    }

    public int size() {
        return cache.size();
    }
}
```

---

## Production Issues Identified

### 1. Memory Leak - Expired Entries Never Removed

**Issue:** The `get()` method checks if entries are expired, but never removes expired entries from the map. Under a load of hundreds of writes per second, the cache grows indefinitely.

**Impact:** Causes `OutOfMemoryError` in production. Memory consumption grows without bound until the application crashes.

**Fix:** Use `cache.remove(key, entry)` in the else block when an expired entry is detected. I am preferring two-argument remove to atomically remove only if the entry hasn't been replaced by another thread.
Can later implement a ExecutorService that periodically sweeps and removes all expired entries.

---

### 2. No Maximum Size or Eviction Policy

**Issue:** The cache has no upper bound on entries, with hundreds of writes per second the cache grows unbounded.

**Impact:** Under sustained load, memory consumption grows without limit, eventually causing OutOfMemoryError. No mechanism exists to prevent the cache from consuming all available heap space.

**Fix:** Have a maximum size limit and an eviction policy (LRU, LFU, etc.)

---

### 3. Non-Monotonic Time Source

**Issue:** `System.currentTimeMillis()` returns wall-clock time, It is not monotonic. Clock adjustments can affect the cache expiry.

**Impact:** If the clock jumps backward, the comparision logic breaks, new entries look old and get rejected, or old entries never expire. If the clock jumps forward, entries expire too early

**Fix:** Use System.nanoTime() instead of currentTimeMillis() for measuring elapsed time. It always increases and isn't affected by clock changes.

---

### 4. Misleading size() Method

**Issue:** The `size()` method returns total entries including expired ones, since expired entries are never removed.

**Impact:** Reported size does not reflect actual number of valid cached entries.

**Fix:** Fixing first issue should auto fix this.

---

### Note: Eventual Consistency (Observation)

The get() method allows stale reads if updates happen concurrently. This is acceptable at this scale (thousands of reads/sec), enforcing strict consistency via locking would kill throughput.