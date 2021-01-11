package cpen221.mp3;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.Cacheable;
import cpen221.mp3.wikimediator.NotInCacheException;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class CacheTests {

    public class CacheItemTest implements Cacheable {
        private String id;
        private String item;

        public CacheItemTest(String item) {
            this.item = item;
            id = String.valueOf(item.hashCode());
        }

        public String id() {
            return id;
        }
    }

    @Test
    public void testPut() {
        Cache cache = new Cache();
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");
        CacheItemTest cacheItemTest2 = new CacheItemTest("2");
        CacheItemTest cacheItemTest3 = new CacheItemTest("3");
        CacheItemTest cacheItemTest4 = new CacheItemTest("4");

        cache.put(cacheItemTest1);
        cache.put(cacheItemTest2);
        cache.put(cacheItemTest3);
        cache.put(cacheItemTest4);

        Set<CacheItemTest> stringSet = new HashSet<>();

        stringSet.add(cacheItemTest1);
        stringSet.add(cacheItemTest2);
        stringSet.add(cacheItemTest3);
        stringSet.add(cacheItemTest4);

        Assert.assertEquals(stringSet, cache.keySet());
    }

    @Test
    public void testPutExists() {
        Cache cache = new Cache();
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");

        cache.put(cacheItemTest1);

        Assert.assertFalse(cache.put(cacheItemTest1));
    }

    @Test
    public void testPutFull() {
        Cache cache = new Cache(3, 10000);
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");
        CacheItemTest cacheItemTest2 = new CacheItemTest("2");
        CacheItemTest cacheItemTest3 = new CacheItemTest("3");
        CacheItemTest cacheItemTest4 = new CacheItemTest("4");


        cache.put(cacheItemTest1);

        try {
            Thread.sleep(400);
        }catch (InterruptedException e) {

        }

        cache.put(cacheItemTest2);

        try {
            Thread.sleep(400);
        }catch (InterruptedException e) {

        }
        cache.put(cacheItemTest3);

        try {
            Thread.sleep(400);
        }catch (InterruptedException e) {

        }

        cache.put(cacheItemTest4);

        Set<CacheItemTest> stringSet = new HashSet<>();

        stringSet.add(cacheItemTest2);
        stringSet.add(cacheItemTest3);
        stringSet.add(cacheItemTest4);

        Assert.assertEquals(stringSet, cache.keySet());
    }

    @Test
    public void testPruneMap() {
        Cache cache = new Cache(10, 3);
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");
        CacheItemTest cacheItemTest2 = new CacheItemTest("2");
        CacheItemTest cacheItemTest3 = new CacheItemTest("3");
        CacheItemTest cacheItemTest4 = new CacheItemTest("4");


        cache.put(cacheItemTest1);

        try {
            Thread.sleep(4000);
        }catch (InterruptedException e) {

        }

        cache.put(cacheItemTest2);
        cache.put(cacheItemTest3);
        cache.put(cacheItemTest4);

        Set<CacheItemTest> stringSet = new HashSet<>();

        stringSet.add(cacheItemTest2);
        stringSet.add(cacheItemTest3);
        stringSet.add(cacheItemTest4);


        Assert.assertEquals(stringSet, cache.keySet());
    }

    @Test
    public void testGet() throws NotInCacheException{
        Cache cache = new Cache();
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");
        CacheItemTest cacheItemTest2 = new CacheItemTest("2");
        CacheItemTest cacheItemTest3 = new CacheItemTest("3");
        CacheItemTest cacheItemTest4 = new CacheItemTest("4");


        cache.put(cacheItemTest1);
        cache.put(cacheItemTest2);
        cache.put(cacheItemTest3);
        cache.put(cacheItemTest4);

        Assert.assertEquals(cacheItemTest1, cache.get(cacheItemTest1.id()));
    }

    @Test(expected = NotInCacheException.class)
    public void testGetException() throws NotInCacheException{
        Cache cache = new Cache(10, 3);
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");
        CacheItemTest cacheItemTest2 = new CacheItemTest("2");
        CacheItemTest cacheItemTest3 = new CacheItemTest("3");
        CacheItemTest cacheItemTest4 = new CacheItemTest("4");


        cache.put(cacheItemTest1);

        try {
            Thread.sleep(4000);
        }catch (InterruptedException e) {

        }

        cache.put(cacheItemTest2);
        cache.put(cacheItemTest3);
        cache.put(cacheItemTest4);

        Assert.assertEquals(cacheItemTest1, cache.get(cacheItemTest1.id()));
    }

    @Test
    public void testTouch() {
        Cache cache = new Cache(10, 3);
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");
        CacheItemTest cacheItemTest2 = new CacheItemTest("2");
        CacheItemTest cacheItemTest3 = new CacheItemTest("3");
        CacheItemTest cacheItemTest4 = new CacheItemTest("4");

        cache.put(cacheItemTest1);

        try {
            Thread.sleep(2900);
        }catch (InterruptedException e) {

        }

        cache.put(cacheItemTest2);
        cache.put(cacheItemTest3);
        cache.put(cacheItemTest4);

        Assert.assertTrue(cache.touch(cacheItemTest1.id()));
    }

    @Test
    public void testTouchNullCase() {
        Cache cache = new Cache(10, 3);
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");
        CacheItemTest cacheItemTest2 = new CacheItemTest("2");
        CacheItemTest cacheItemTest3 = new CacheItemTest("3");
        CacheItemTest cacheItemTest4 = new CacheItemTest("4");

        cache.put(cacheItemTest1);

        try {
            Thread.sleep(3100);
        }catch (InterruptedException e) {

        }

        cache.put(cacheItemTest2);
        cache.put(cacheItemTest3);
        cache.put(cacheItemTest4);

        Assert.assertFalse(cache.touch(cacheItemTest1.id()));
    }

    @Test
    public void testUpdate() {
        Cache cache = new Cache(10, 3);
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");
        CacheItemTest cacheItemTest2 = new CacheItemTest("2");
        CacheItemTest cacheItemTest3 = new CacheItemTest("3");
        CacheItemTest cacheItemTest4 = new CacheItemTest("4");

        cache.put(cacheItemTest1);

        try {
            Thread.sleep(2900);
        }catch (InterruptedException e) {

        }

        cache.put(cacheItemTest2);
        cache.put(cacheItemTest3);
        cache.put(cacheItemTest4);

        Assert.assertTrue(cache.update(cacheItemTest1));

    }

    @Test
    public void testUpdateNullCase() {
        Cache cache = new Cache(10, 3);
        CacheItemTest cacheItemTest1 = new CacheItemTest("1");
        CacheItemTest cacheItemTest2 = new CacheItemTest("2");
        CacheItemTest cacheItemTest3 = new CacheItemTest("3");
        CacheItemTest cacheItemTest4 = new CacheItemTest("4");

        cache.put(cacheItemTest1);

        try {
            Thread.sleep(3100);
        }catch (InterruptedException e) {

        }

        cache.put(cacheItemTest2);
        cache.put(cacheItemTest3);
        cache.put(cacheItemTest4);

        Assert.assertFalse(cache.update(cacheItemTest1));
    }

}
