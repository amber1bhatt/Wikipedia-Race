package cpen221.mp3.wikimediator;

import fastily.jwiki.dwrap.Contrib;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.Cacheable;
import fastily.jwiki.core.Wiki;
import org.antlr.v4.runtime.misc.Pair;

/**
 * A class that utilizes the JWiki API to execute various queries on the en.wikipedia.org domain
 * The class also stores statistical data on the frequency of common requests.
 * Upon initialization, a thread is started to collect statistical data in 30
 * second intervals.
 */

public class WikiMediator {

    /**
     * A class that contains relevant information about any request made to WikiMediator to be stored in the cache
     * @param <V> is a generic object that represents the return value for a request
     */
    public class CacheItem<V> implements Cacheable {
        private final String id;
        private final V item;
        private final AtomicInteger count;
        private final String query;
        private final String type;

        //  Representation Invariants:
        //      - id must be a unique identifier for item
        //      - type must be one of the predefined operations possible for
        //        wikiMediator: getPage, simpleSearch, or getConnectedPages
        //      - count must be >= 1 // ask about this field, and the way its incremented
        //
        //  Abstraction Function:
        //      Represents a data type that holds an item returned from wikiMediator functions
        //      getPage, simpleSearch, or getConnectedPages and to be stored in a cache. id represents
        //      the unique identification value associated with an item. Query represents the value
        //      passed into wikiMediator functions getPage, simpleSearch, or getConnectedPages to return item.
        //      type represents the function called (getPage, simpleSearch, or getConnectedPages) to return item.
        //      count represents the number of times the
        //
        // Thread safety argument:
        //      This class is Thread-safe because:
        //      - id, query, item and type are final
        //      - count is a thread safe type AtomicInteger and no race conditions exist
        //      - item points to generic type which may be mutable but is never mutated within the class

        /**
         * Create an item which implements cache and has helper fields
         * for wikiMediator
         *
         * @param item a generic object
         * @param id the id of the item
         * @param query a collection of words to be addressed to Wikipedia
         * @param type the type of search
         */
        public CacheItem(V item, int id, String query, String type) {
            count = new AtomicInteger(1);
            this.item = item;
            this.query = query;
            this.id = String.valueOf(id);
            this.type = type;
        }

        /**
         * @return the item
         */
        public V getItem() {
            V i = item;
            return i;
        }

        /**
         * @return the id associated with the item
         */
        public String id() {
            return id;
        }

        /**
         * @return the count
         */
        public Integer getCount() {
            return count.get();
        }

        /**
         * Used to increment the count by 1
         */
        public void incrementCount() {
            count.incrementAndGet();
        }

        /**
         * @return the query associated with item
         */
        public String getQuery() {
            return query;
        }

        /**
         * @return the type of request
         */
        public String getType() {
            return type;
        }
    }


    private static final int THIRTY_SECS_MILLI = 30000;
    private static final String SIMPLE_SEARCH = "simpleSearch";
    private static final String GET_PAGE = "getPage";
    private static final String GET_CONNECTED_PAGES = "getConnectedPages";

    private final Cache<CacheItem<String>> cacheGetPage;
    private final Cache<CacheItem<List<String>>> cacheSimpleSearch;
    private final Cache<CacheItem<List<String>>> cacheGetConnectedPage;
    private final Map<CacheItem, Long> wikiMap;
    private WikiStatistics wikiStat;
    private final Wiki wiki;
    private AtomicInteger requestCount;
    private AtomicInteger maxRequestCount;

    //  Representation Invariants:
    //      - cacheGetPage, cacheSimpleSearch, cacheGetConnectedPage, and wikiList cannot contain null entries
    //      - cacheGetConnectedPage contains only results returned from the getConnectedPages function
    //      - cacheGetPage contains only results returned from the getPages function
    //      - cacheSimpleSearch contains only results returned from the simpleSearch function
    //      - Every key and value in wikiMap must share the relationship: value = the time when key was added to map
    //      - wikiMap must contain every request from only getConnectedPages, getPages, and simpleSearch
    //      - requestCount contains the number of requests from every public method in an instance of wikiMediator
    //        in every consecutive 30 second window since the instantiation of this class
    //      - maxRequestCount contains the maximum value requestCount since the instantiation of this class
    //      - wiki must be linked to the en.wikipedia.org domain
    //
    //  Abstraction Function:
    //      Represents a data type that processes various requests to en.wikipedia.org through the JWiki API and collects
    //      statistical data on the requests. cacheGetConnectedPage, cacheGetPage, and cacheSimpleSearch maintain caches of the requests
    //      from their respective functions. wikiMap represents all requests processed by WikiMediator and the time it was processed.
    //      wiki represents a __. requestCount represents the number of requests for the functions getConnectedPages, getPages
    //      simpleSearch, zeitgeist, and trending in every consecutive 30 second window since the instantiation of this class. maxRequestCount
    //      represents the the maximum value requestCount since the instantiation of this class.
    //
    // Thread safety argument:
    //      This class is Thread-safe because:
    //      - because all modifications of state have synchronized locks
    //      - thread-safe types ConcurrentHashMap, CacheItem, Cache, and Atomic Integer are used

    /**
     * Initialize the cache to have the default capacity and timeout
     * Initialize the wiki to reference the domain of Wikipedia
     * Initialize
     * Initialize a counter for the amount of requests, requestCount, and the maximum amount of requests, maxRequestCount
     * as 0
     * Start ThirtySecCount() thread
     */
    public WikiMediator() {
        cacheGetPage = new Cache<>();
        cacheSimpleSearch = new Cache<>();
        cacheGetConnectedPage = new Cache<>();
        wiki = new Wiki("en.wikipedia.org");
        wikiMap = new ConcurrentHashMap<>();
        wikiStat = new WikiStatistics();
        requestCount = new AtomicInteger(0);
        maxRequestCount = new AtomicInteger(0);
        ThirtySecCount();
    }

    public WikiMediator(WikiStatistics wikiStat, int maxRequestCount) {
        cacheGetPage = new Cache<>();
        cacheSimpleSearch = new Cache<>();
        cacheGetConnectedPage = new Cache<>();
        wiki = new Wiki("en.wikipedia.org");
        wikiMap = new ConcurrentHashMap<>();
        this.wikiStat = wikiStat;
        requestCount = new AtomicInteger(0);
        this.maxRequestCount = new AtomicInteger(maxRequestCount);
        ThirtySecCount();
    }

    /**
     * Thread which compares the value of requestCount and maxRequestCount every 30 seconds
     * and updates the value of maxRequestCount to be equal to requestCount if requestCount
     * is greater than maxRequestCount.
     *
     * Prints the stack trace in the case of an InterruptedException
     */
    public void ThirtySecCount() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (this) {
                        int currReq = requestCount.get();
                        int currMax = maxRequestCount.get();
                        if (currReq > currMax) {
                            maxRequestCount.compareAndExchange(currMax, currReq);
                        }
                        requestCount.compareAndSet(currReq, 0);
                    }
                    try {
                        Thread.sleep(THIRTY_SECS_MILLI);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * Given a query, find up to limit page titles that match the query string
     * If the item is not in the cache, adds it to the cache. Utilizes Jwiki
     * search method.
     *
     * @param query a collection of words to be addressed to Wikipedia. query
     *        must not be an empty string.
     * @param limit the maximum amount of page titles to request, must be greater than or equal to 0
     * @return a list of page titles that match the query string. List will be empty
     *         if no pages match query.
     */
    public List<String> simpleSearch(String query, int limit) {
        requestCount.getAndIncrement();
        wikiStat.addRequest(new Pair<>(query, String.valueOf(System.currentTimeMillis())));
        wikiStat.setCount(maxRequestCount.intValue(), requestCount.intValue());

        try {
            FileOutputStream fos = new FileOutputStream("local\\local.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(wikiStat);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            synchronized (this) {
                CacheItem<List<String>> val = cacheSimpleSearch.get(String.valueOf(query.hashCode()));
                cacheSimpleSearch.touch(val.id());
                wikiMap.replace(val, System.currentTimeMillis());
                val.incrementCount();
                return val.getItem();
            }
        } catch (NotInCacheException e) {
            synchronized (this) {
                List<String> res = wiki.search(query, limit);
                Collections.sort(res);
                CacheItem<List<String>> val = new CacheItem(res, query.hashCode(), query, SIMPLE_SEARCH);
                cacheSimpleSearch.put(val);
                wikiMap.put(val, System.currentTimeMillis());
                return val.getItem();
            }
        }
    }

    /**
     * Given a pageTitle, find the text associated with the Wikipedia page that matches pageTitle.
     * If the item is not in the cache, add it in the cache. Utilizes JWiki getPageText method.
     *
     * @param pageTitle a string which represents the title of a specific page. pageTitle
     *        must not be an empty string.
     * @return return the text associated with the Wikipedia page that matches pageTitle.
     *         will return empty string if pageTitle matches no pages.
     */
    public String getPage(String pageTitle) {
        requestCount.getAndIncrement();
        wikiStat.addRequest(new Pair<>(pageTitle, String.valueOf(System.currentTimeMillis())));
        wikiStat.setCount(maxRequestCount.intValue(), requestCount.intValue());

        try {
            synchronized (this) {
                CacheItem<String> val = cacheGetPage.get(String.valueOf(pageTitle.hashCode()));
                cacheGetPage.touch(val.id());
                wikiMap.replace(val, System.currentTimeMillis());
                val.incrementCount();
                return val.getItem();
            }
        } catch (NotInCacheException e) {
            synchronized (this) {
                CacheItem<String> val = new CacheItem(wiki.getPageText(pageTitle), pageTitle.hashCode(), pageTitle, GET_PAGE);
                cacheGetPage.put(val);
                wikiMap.put(val, System.currentTimeMillis());
                return val.getItem();
            }
        }

    }

    /**
     * Given the pageTitle and the amount of hops, find a list of page titles that can be reached by following
     * up to hops links starting with the page specified by pageTitle
     * If the item is not in the cache, add it in the cache
     * Utilizes Jwiki getLinksOnPage method.
     *
     * @param pageTitle a string which represents the title of a specific page. pageTitle
     *        must not be an empty string.
     * @param hops represents the depth of links we follow with the root being all the links on the page of pageTitle. hops must be
 *            greater than or equal to 0.
     * @return a list of page titles that can be reached by following up to hops links starting with the page specified by pageTitle
     *         list will only contain pageTitle if hops is equal to 0.
     */
    public List<String> getConnectedPages(String pageTitle, int hops) {
        requestCount.getAndIncrement();
        Set<String> included = new HashSet<>();

        try {
            synchronized (this) {
                CacheItem<List<String>> val = cacheGetConnectedPage.get(String.valueOf(pageTitle.hashCode() * hops));
                cacheGetConnectedPage.touch(val.id());
                wikiMap.replace(val, System.currentTimeMillis());
                val.incrementCount();
                return val.getItem();
            }
        } catch (NotInCacheException e) {
            getConnectedPagesRecursive(pageTitle, hops, included);
            synchronized (this) {
                CacheItem<List<String>> val = new CacheItem(new ArrayList<>(included), pageTitle.hashCode() * hops, pageTitle, GET_CONNECTED_PAGES);
                cacheGetConnectedPage.put(val);
                wikiMap.put(val, System.currentTimeMillis());
                return val.getItem();
            }
        }

    }

    /**
     * Recursive helper method for get connected pages
     *
     * @param pageTitle a string which represents the title of a specific page. pageTitle
     *        must not be an empty string.
     * @param hops represents the depth of links we follow with the root being all the links on the page of pageTitle. hops must be
     *        greater than or equal to 0.
     * @param included a set of page titles that can be reached by following up to hops links starting with the first page title in
     *        this set
     */
    private void getConnectedPagesRecursive(String pageTitle, int hops, Set<String> included) {
        included.add(pageTitle);

        if (hops == 0) {
            return;
        }

        List<String> links = wiki.getLinksOnPage(pageTitle);

        for (String title : links) {
            if (!included.contains(title)) {
                getConnectedPagesRecursive(title, hops - 1, included);
            }
        }
    }

    /**
     * Find the most common strings used in simpleSearch and getPage requests out of all requests in wikiMap, with
     * items sorted in non-increasing count order.
     *
     * @param limit the maximum size of the list returned, must be greater than or equal to 0. If limit is less than
     *        the size of the return list, the entire list will be returned.
     * @return the most common strings used in simpleSearch and getPage requests, with items sorted in non-increasing count order.
     *         If more requests than limit have been made, only return up to limit items
     */
    public synchronized List<String> zeitgeist(int limit) {
        requestCount.getAndIncrement();

//        List<CacheItem> list = filterSortRequests();
//        List<String> zeitListString = list.stream()
//                .map(i -> i.getQuery())
//                .distinct()
//                .collect(Collectors.toList());

        Map<String, Integer> reqCount = new HashMap<>();
        for (Pair<String, String> p : wikiStat.getRequests()) {
            if (reqCount.containsKey(p.a)) {
                reqCount.replace(p.a, reqCount.get(p.a) + 1);
            } else {
                reqCount.put(p.a, 1);
            }
        }

        List<String> zeitListString = sortRequests(reqCount);

        if (zeitListString.size() > limit) {
            return zeitListString.subList(0, limit);
        } else {
            return zeitListString;
        }
    }

    /**
     * Find the most common strings used in simpleSearch and getPage requests out of all requests in wikiMap, with
     * items sorted in non-increasing count order made within the last 30 seconds.
     *
     * @param limit the maximum amount of items at any given instance, must be greater than or equal to 0. If limit is less than
     *        the size of the return list, the entire list will be returned.
     * @return the most frequent requests made in the last 30 seconds simpleSearch and getPage requests, with items sorted
     *         in non-increasing count order. If more requests than limit have been made, only return up to limit items.
     */
    public List<String> trending(int limit) {
        requestCount.getAndIncrement();
//
//        List<CacheItem> list = filterSortRequests();
//        List<String> trendListString = list.stream()
//                .filter(i -> THIRTY_SECS_MILLI >= (System.currentTimeMillis() - wikiMap.get(i)))
//                .map(i -> i.getQuery())
//                .distinct()
//                .collect(Collectors.toList());
//
        Map<String, Integer> reqCount = new HashMap<>();
        for (Pair<String, String> p : wikiStat.getRequests()) {
            if (System.currentTimeMillis() - Long.parseLong(p.b) <= THIRTY_SECS_MILLI){
                if (reqCount.containsKey(p.a)) {
                    reqCount.replace(p.a, reqCount.get(p.a) + 1);
                } else {
                    reqCount.put(p.a, 1);
                }
            }
        }

        List<String> trendListString = sortRequests(reqCount);

        if (trendListString.size() > limit) {
            return trendListString.subList(0, limit);
        } else {
            return trendListString;
        }
    }

    /**
     * Helper method for trending and zeitgeist which handles common sorting and filtering
     */
    private List<String> sortRequests(Map<String, Integer> reqCount) {
        List<String> sortList = reqCount.keySet().stream()
                .sorted((p1, p2) -> reqCount.get(p2) - reqCount.get(p1))
                .collect(Collectors.toList());
        return sortList;
    }

    /**
     * @return maximum number of requests made in any 30 seconds
     */
    public synchronized int peakLoad30s() {
        requestCount.getAndIncrement();

        int currReq = requestCount.get();
        int currMax = maxRequestCount.get();
        if (currReq > currMax) {
            maxRequestCount.compareAndExchange(currMax, currReq);
        }

        return maxRequestCount.get();
    }

    /**
     * Find the path of pages it takes to get from startPage to stopPage
     *
     * @param startPage the root page to begin the path
     * @param stopPage the destination page at the end of the path
     * @return list of pages to get from startPage to stopPage
     */
    public List<String> getPath(String startPage, String stopPage) {
        requestCount.getAndIncrement();

        Set<String> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();

        queue.add(new Node(startPage, null));

        if(startPage.equals(stopPage)) {
            List<String> samePage = new ArrayList<>();
            samePage.add(startPage);
            return samePage;
        }

        long startTime = System.currentTimeMillis();

        while(!queue.isEmpty()) {
            Node current = queue.poll();

            long currentTime = System.currentTimeMillis();

            if(currentTime - startTime > 290000) {
                return new ArrayList<>();
            }

            if(!visited.contains(current.getNode())) {
                for(String s : getConnectedPages(current.getNode(), 1)) {
                    Node child = new Node(s, current);

                    if(s.equals(stopPage)) {
                        return buildPath(child);
                    }
                    queue.add(child);
                }
                visited.add(current.getNode());
            }
        }
        return null;
    }

    /**
     * Helper method to getPath which builds the path from startPage to stopPage
     * @param child, of type Node that represents the child of the most recent parent page
     * @return an arraylist containing the full path from startPage to stopPage
     */
    private List<String> buildPath(Node child) {
        Stack<String> stack = new Stack<>();
        Node current = child;
        while (current != null) {
            stack.push(current.getNode());
            current = current.getParent();
        }
        List<String> path = new ArrayList<>();
        while (!stack.isEmpty()) {
            path.add(stack.pop());
        }
        return path;
    }

    /**
     * A private class used only for methods buildPath and getPath which
     * helps keep track of parents and their respective children.
     */
    private static class Node {
        private final Node parent;
        private final String node;

        Node(String node, Node parent) {
            this.parent = parent;
            this.node = node;
        }

        Node getParent() {
            return parent;
        }

        String getNode() {
            return node;
        }
    }


    /**
     * Execute the client-specified structured query
     *
     * @param query a client-specified request
     *        query must not be an empty string.
     *        query must follow the grammar specified in the project description.
     * @return the response from the server taken a structured query as input
     */
    public List<String> excuteQuery(String query) throws InvalidQueryException {
        requestCount.getAndIncrement();
        Query que = QueryFactory.parse(query);
        List<Condition> condList = que.getConditions();

        List<String> result = new ArrayList<>();
        for (Condition cond : condList) {
            String val = cond.getQuery().substring(1, cond.getQuery().length() - 1);
            List<String> interRes = performCond(cond.getItem(), val);
            if (condList.size() > 1) {
                if (cond.getOperator() == "and") {
                    result.retainAll(interRes);
                } else if (cond.getOperator() == "or") {
                    result.addAll(interRes);
                }
            } else {
                result = interRes;
            }
        }

        return performQuery(que.getItem(), que.getSorted(), result);
    }

    private List<String> performQuery(String item, String sorted, List<String> list) throws InvalidQueryException {
        switch(item) {
            case "page":
                return list;
            case "author":
                List<String> authRes = list.stream().map(q -> wiki.getLastEditor(q)).distinct().collect(Collectors.toList());
                return authRes;
            case "category":
                List<String> catRes = list.stream().map(q -> wiki.getCategoriesOnPage(q)).flatMap(List::stream).distinct().collect(Collectors.toList());
                return catRes;
        }

        throw new InvalidQueryException();
    }

    private ArrayList<String> performCond(String item, String query) throws InvalidQueryException {
        switch(item) {
            case "title":
                return wiki.search(query, -1);
            case "author":
                ArrayList<String> res = new ArrayList<>();
                ArrayList<Contrib> contributions = wiki.getContribs(query,  -1, false);
                for (Contrib c : contributions) {
                    if(wiki.getLastEditor(c.title) == query) {
                        res.add(c.title);
                    }
                }
                return res;
            case "category":
                return wiki.getCategoryMembers(query);
        }

        throw new InvalidQueryException();
    }

}
