package cpen221.mp3.cache;

import cpen221.mp3.wikimediator.NotInCacheException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a time-based-expiring collection of objects.
 * Upon initialization, a thread is started to continuously check
 * the amount of time every object has remained in the cache and remove
 * any objects that have remained longer than expected and become stale.
 *
 * @param <T> represents a Cacheable type
 */

public class Cache<T extends Cacheable> {

    /* the default cache size is 32 objects */
    public static final int DSIZE = 32;
    /* the default timeout value is 3600s */
    public static final int DTIMEOUT = 3600;

    private final int timeout;
    private final int capacity;
    private final Map<String, T> cacheMap = new ConcurrentHashMap<>();
    private final Map<T, Long> cache = new ConcurrentHashMap<>();

    //  Representation Invariant:
    //      - No entries in cacheMap may be null
    //      - Every key and value in cacheMap must share the relationship: key = value.id()
    //      - No entries in cache may be null
    //      - No values in cache can exceed the value timeout
    //      - Every key and value in cache must share the relationship: value = the time when key was added to cache
    //      - cacheMap and cache size must be equal to each other and equal to or less than the capacity
    //
    //  Abstraction Function:
    //      Represents a data type to store capacity objects for a timeout period of time using a polling thread.
    //      cacheMap relates each object with a unique identifier and cache associates every object with
    //      the time at which it was placed into the cache.
    //
    // Thread safety argument:
    //      This class is Thread-safe because:
    //      - cache, cacheMap, timeout and capacity are final
    //      - cache and cacheMap are a thread safe type ConcurrentHashMap
    //      - all public methods have been made synchronized

    /**
     * Create a cache with a fixed capacity and a timeout value.
     * Objects in the cache that have not been refreshed within the timeout period
     * are removed from the cache.
     *
     * @param capacity the number of objects the cache can hold
     * @param timeout  the duration an object should be in the cache before it times out
     */
    public Cache(int capacity, int timeout) {
        this.timeout = timeout*1000;
        this.capacity = capacity;
        pruneThread();
    }

    /**
     * Create a cache with default capacity and timeout values.
     */
    public Cache() {
        this(DSIZE, DTIMEOUT);
    }

    /**
     * Thread which continuously compares the amount of time the object has remained in the cache
     * by checking the value associated with each object key in cache. Thread removes any objects from
     * cache and cacheMap if the value exceeds timeout.
     */
    void pruneThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (this) {
                        long currentTime = System.currentTimeMillis();
                        for (T val : cache.keySet()) {
                            if (currentTime > cache.get(val) + timeout) {
                                cache.remove(val);
                                cacheMap.remove(val.id());
                            }
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * Add an object t to the cache.
     * If the cache is full then remove the least recently accessed object to
     * make room for the new object.
     *
     * @param t the object to put in cache
     * @returns true if t was successfully added to cache
     * @returns false if t is already in cache
     */
    public synchronized boolean put(T t) {
        if(cache.containsKey(t)) {
            return false;
        }

        if (cache.keySet().size() == capacity) {
            List<T> list = new ArrayList<T>(cache.keySet());
            Collections.sort(list, (t1, t2) -> {return Long.compare(cache.get(t1),cache.get(t2));});
            cache.remove(list.get(0));
            cacheMap.remove(list.get(0).id());
        }

        cacheMap.putIfAbsent(t.id(), t);
        cache.putIfAbsent(t, System.currentTimeMillis());
        return true;
    }

    /**
     * retrieves an object with the associated identifier id from the cache
     *
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the cache
     * @throws NotInCacheException if id is not in cacheMap
     */
    public synchronized T get(String id) throws NotInCacheException {
        T ret = cacheMap.get(id);

        if (ret == null) {
            throw new NotInCacheException();
        }

        return ret;
    }

    /**
     * Update the time value for the object with the provided id to the current time.
     * This method is used to mark an object as "not stale" so that its timeout
     * is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful
     * @return false if object with id is not within cache
     */
    public synchronized boolean touch(String id) {
        T ret = cacheMap.get(id);

        if (ret == null) {
            return false;
        }

        cache.replace(ret, System.currentTimeMillis());
        return true;
    }

    /**
     * Update an object in the cache by removing the object and
     * placing the updated object back in the cache
     *
     * @param t the object to update
     * @return true if successful
     * @return false if object is not within cache
     */
    public synchronized boolean update(T t) {
        T ret = cacheMap.get(t.id());

        if (ret == null) {
            return false;
        }

        cache.remove(t);
        cacheMap.remove(t.id());

        put(t);
        return true;
    }

    /**
     * method used exclusively for testing. Should be set to private after successful testing.
     *
     * @return the set of CacheItem keys in the cache ConcurrentHashMap
     */
    public Set<T> keySet() {
        // defensive copying
        return new HashSet<>(cache.keySet());
    }
}
