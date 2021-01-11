package cpen221.mp3;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class ServerTests {

    public class Server implements Runnable {
        Integer n;

        public Server(Integer n) {
            this.n = n;
        }

        public void run() {
            try {
                WikiMediatorServer server = new WikiMediatorServer(WikiMediatorServer.WIKI_MEDIATOR_PORT, n);
                server.serve();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class Client implements Runnable {
        private String serverReply;
        private String id;
        private String type;
        private String query;
        private int hops;
        private int limit;
        private String timeout;

        public Client withId(String id) {
            this.id = id;
            return this;
        }

        public Client withType(String type) {
            this.type = type;
            return this;
        }

        public Client withQuery(String query) {
            this.query = query;
            return this;
        }

        public Client withHops(int hops) {
            this.hops = hops;
            return this;
        }

        public Client withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Client withTimeout(String timeout) {
            this.timeout = timeout;
            return this;
        }

        @Override
        public void run() {
            try {
                runClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void runClient() throws IOException {
            WikiMediatiorClient client = new WikiMediatiorClient("localhost", WikiMediatorServer.WIKI_MEDIATOR_PORT);

            switch (type) {
                case "simpleSearch":
                    if (timeout != null) {
                        client.sendRequest(id, type, query, limit, timeout);
                    } else {
                        client.sendRequest(id, type, query, limit);
                    }
                    break;

                case "getPage":
                    if (timeout != null) {
                        client.sendGetPage(id, type, query, timeout);
                    } else {
                        client.sendGetPage(id, type, query);
                    }
                    break;

                case "getConnectedPages":
                    if (timeout != null) {
                        client.sendConnectedPages(id, type, query, hops, timeout);
                    } else {
                        client.sendConnectedPages(id, type, query, hops);
                    }
                    break;

                case "trending":
                    if (timeout != null) {
                        client.sendTrending(id, type, limit, timeout);
                    } else {
                        client.sendTrending(id, type, limit);
                    }
                    break;

                case "zeitgeist":
                    if (timeout != null) {
                        client.sendZeitgeist(id, type, limit, timeout);
                    } else {
                        client.sendZeitgeist(id, type, limit);
                    }
                    break;

                case "peakLoad30s":
                    client.sendPeakLoad30s(id, type);
                    break;
            }

            serverReply = client.getReply();
            client.close();
        }

        public String getServerReply() {
            return serverReply;
        }

    }

    @Test
    public void getSimpleSearchTest1() throws InterruptedException {
        Server rs = new Server(1);
        Thread server = new Thread(rs);
        server.start();

        Client rc = new Client().withId("1").withType("simpleSearch").withQuery("Jake Paul").withLimit(12);
        Thread client = new Thread(rc);
        client.start();

        server.join();
        client.join();

        String serverReply = rc.getServerReply();
        WikiMediator wm = new WikiMediator();
        JsonObject reply = new JsonObject();

        reply.addProperty("id", "1");
        reply.addProperty("status", "succeeded");
        reply.addProperty("response", wm.simpleSearch("Jake Paul", 12).toString());

        assertEquals(serverReply, reply.toString());
    }

    @Test
    public void getPageTest1() throws InterruptedException, IOException {
        Server rs = new Server(1);
        Thread server = new Thread(rs);
        server.start();

        Client rc = new Client().withId("1").withType("getPage").withQuery("Barack Obama");
        Thread client = new Thread(rc);
        client.start();

        server.join();
        client.join();

        String serverReply = rc.getServerReply();
        WikiMediator wm = new WikiMediator();
        JsonObject reply = new JsonObject();
        reply.addProperty("id","1");
        reply.addProperty("status","succeeded");
        reply.addProperty("response", wm.getPage("Barack Obama"));

        Assert.assertEquals(serverReply, reply.toString());
    }

    @Test
    public void getPageTest2() throws InterruptedException {
        Server rs = new Server(1);
        Thread server = new Thread(rs);
        server.start();

        Client rc = new Client().withId("1").withType("getPage").withQuery("Bernie Sanders");
        Thread client = new Thread(rc);
        client.start();

        server.join();
        client.join();

        String serverReply = rc.getServerReply();
        WikiMediator wm = new WikiMediator();
        JsonObject reply = new JsonObject();
        reply.addProperty("id", "1");
        reply.addProperty("status", "succeeded");
        reply.addProperty("response", wm.getPage("Bernie Sanders"));

        Assert.assertEquals(serverReply, reply.toString());
    }

    @Test
    public void getConnectedPageTest1() throws InterruptedException {
        Server rs = new Server(1);
        Thread server = new Thread(rs);
        server.start();

        Client rc = new Client().withId("1").withType("getConnectedPages").withQuery("Abu Dhabi Government Media Office(ADGMO)").withHops(2);
        Thread client = new Thread(rc);
        client.start();

        server.join();
        client.join();

        String serverReply = rc.getServerReply();
        WikiMediator wm = new WikiMediator();
        JsonObject reply = new JsonObject();
        reply.addProperty("id", "1");
        reply.addProperty("status", "succeeded");
        reply.addProperty("response", wm.getConnectedPages("Abu Dhabi Government Media Office(ADGMO)", 2).toString());

        Assert.assertEquals(serverReply, reply.toString());
    }

    @Test
    public void getConnectedPageTestTimeout() throws InterruptedException {
        Server rs = new Server(1);
        Thread server = new Thread(rs);
        server.start();

        Client rc = new Client().withId("1").withType("getConnectedPages").withQuery("Barack Obama").withHops(2).withTimeout("10");
        Thread client = new Thread(rc);
        client.start();

        server.join();
        client.join();

        String serverReply = rc.getServerReply();
        WikiMediator wm = new WikiMediator();
        JsonObject reply = new JsonObject();
        reply.addProperty("id", "1");
        reply.addProperty("status", "failed");
        reply.addProperty("response", "Operation timed out");

        Assert.assertEquals(serverReply, reply.toString());
    }

    @Test
    public void getZeitgeistTest1() throws InterruptedException {
        Server rs = new Server(20);
        Thread server = new Thread(rs);
        server.start();

        int limit = 5;
        String query1 = "Computer Engineering";
        String query2 = "value";
        String query3 = "Number";
        String query4 = "Computer";

        Thread[] t = new Thread[17];

        t[0] = new Thread(new Client().withId("1").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[1] = new Thread(new Client().withId("2").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[2] = new Thread(new Client().withId("3").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[3] = new Thread(new Client().withId("4").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[4] = new Thread(new Client().withId("5").withType("getPage").withQuery(query1));
        t[5] = new Thread(new Client().withId("6").withType("getPage").withQuery(query1));
        t[6] = new Thread(new Client().withId("7").withType("getPage").withQuery(query1));
        t[7] = new Thread(new Client().withId("8").withType("getConnectedPages").withQuery(query2).withHops(0));
        t[8] = new Thread(new Client().withId("9").withType("getPage").withQuery(query2));
        t[9] = new Thread(new Client().withId("10").withType("getPage").withQuery(query2));
        t[10] = new Thread(new Client().withId("11").withType("getConnectedPages").withQuery(query2).withHops(0));
        t[11] = new Thread(new Client().withId("12").withType("getConnectedPages").withQuery(query1).withHops(0));
        t[12] = new Thread(new Client().withId("13").withType("getConnectedPages").withQuery(query1).withHops(0));
        t[13] = new Thread(new Client().withId("14").withType("simpleSearch").withQuery(query3).withLimit(limit));
        t[14] = new Thread(new Client().withId("15").withType("simpleSearch").withQuery(query4).withLimit(limit));
        t[15] = new Thread(new Client().withId("16").withType("simpleSearch").withQuery(query4).withLimit(limit));
        t[16] = new Thread(new Client().withId("17").withType("simpleSearch").withQuery(query4).withLimit(limit));

        for (int i = 0; i <= 16; i++) {
            t[i].start();
        }

        for (int i = 0; i <= 16; i++) {
            t[i].join();
        }

        Client rc = new Client().withId("18").withType("zeitgeist").withLimit(2);
        Thread client = new Thread(rc);
        client.start();

        client.join();

        String serverReply = rc.getServerReply();
        JsonObject reply = new JsonObject();
        reply.addProperty("id", "18");
        reply.addProperty("status", "succeeded");
        reply.addProperty("response", "[Computer Engineering, Computer]");

        Assert.assertEquals(serverReply, reply.toString());
    }

    @Test
    public void getTrendingTest1() throws InterruptedException, IOException {
        Server rs = new Server(20);
        Thread server = new Thread(rs);
        server.start();

        int limit = 5;
        String query1 = "Computer Engineering";
        String query2 = "value";
        String query3 = "Number";
        String query4 = "Computer";

        Thread[] t = new Thread[17];

        t[0] = new Thread(new Client().withId("1").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[1] = new Thread(new Client().withId("2").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[2] = new Thread(new Client().withId("3").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[3] = new Thread(new Client().withId("4").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[4] = new Thread(new Client().withId("5").withType("getPage").withQuery(query1));
        t[5] = new Thread(new Client().withId("6").withType("getPage").withQuery(query1));
        t[6] = new Thread(new Client().withId("7").withType("getPage").withQuery(query1));
        t[7] = new Thread(new Client().withId("8").withType("getConnectedPages").withQuery(query2).withHops(0));

        t[8] = new Thread(new Client().withId("9").withType("getPage").withQuery(query2));
        t[9] = new Thread(new Client().withId("10").withType("getPage").withQuery(query2));
        t[10] = new Thread(new Client().withId("11").withType("getConnectedPages").withQuery(query2).withHops(0));
        t[11] = new Thread(new Client().withId("12").withType("getConnectedPages").withQuery(query1).withHops(0));
        t[12] = new Thread(new Client().withId("13").withType("getConnectedPages").withQuery(query1).withHops(0));
        t[13] = new Thread(new Client().withId("14").withType("simpleSearch").withQuery(query3).withLimit(limit));
        t[14] = new Thread(new Client().withId("15").withType("simpleSearch").withQuery(query4).withLimit(limit));
        t[15] = new Thread(new Client().withId("16").withType("simpleSearch").withQuery(query4).withLimit(limit));
        t[16] = new Thread(new Client().withId("17").withType("simpleSearch").withQuery(query4).withLimit(limit));

        for (int i = 0; i <= 7; i++) {
            t[i].start();
        }

        for (int i = 0; i <= 7; i++) {
            t[i].join();
        }

        Thread.sleep(30000);

        for (int i = 8; i <= 16; i++) {
            t[i].start();
        }

        for (int i = 8; i <= 16; i++) {
            t[i].join();
        }

        Client rc = new Client().withId("18").withType("trending").withLimit(2);
        Thread client = new Thread(rc);
        client.start();

        client.join();

        String serverReply = rc.getServerReply();
        JsonObject reply = new JsonObject();

        reply.addProperty("id", "18");
        reply.addProperty("status", "succeeded");
        reply.addProperty("response", "[Computer, value]");

        assertEquals(serverReply, reply.toString());
    }

    @Test
    public void peakLoad30sTest1() throws InterruptedException, IOException {
        Server rs = new Server(20);
        Thread server = new Thread(rs);
        server.start();

        int limit = 5;
        String query1 = "Computer Engineering";
        String query2 = "value";
        String query3 = "Number";
        String query4 = "Computer";

        Thread[] t = new Thread[17];

        t[0] = new Thread(new Client().withId("1").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[1] = new Thread(new Client().withId("2").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[2] = new Thread(new Client().withId("3").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[3] = new Thread(new Client().withId("4").withType("simpleSearch").withQuery(query1).withLimit(limit));
        t[4] = new Thread(new Client().withId("5").withType("getPage").withQuery(query1));
        t[5] = new Thread(new Client().withId("6").withType("getPage").withQuery(query1));
        t[6] = new Thread(new Client().withId("7").withType("getPage").withQuery(query1));
        t[7] = new Thread(new Client().withId("8").withType("getConnectedPages").withQuery(query2).withHops(0));
        t[8] = new Thread(new Client().withId("9").withType("getPage").withQuery(query2));

        t[9] = new Thread(new Client().withId("10").withType("getPage").withQuery(query2));
        t[10] = new Thread(new Client().withId("11").withType("getConnectedPages").withQuery(query2).withHops(0));
        t[11] = new Thread(new Client().withId("12").withType("getConnectedPages").withQuery(query1).withHops(0));
        t[12] = new Thread(new Client().withId("13").withType("getConnectedPages").withQuery(query1).withHops(0));
        t[13] = new Thread(new Client().withId("14").withType("simpleSearch").withQuery(query3).withLimit(limit));
        t[14] = new Thread(new Client().withId("15").withType("simpleSearch").withQuery(query4).withLimit(limit));
        t[15] = new Thread(new Client().withId("16").withType("simpleSearch").withQuery(query4).withLimit(limit));
        t[16] = new Thread(new Client().withId("17").withType("simpleSearch").withQuery(query4).withLimit(limit));

        for (int i = 0; i <= 8; i++) {
            t[i].start();
        }

        for (int i = 0; i <= 8; i++) {
            t[i].join();
        }

        Thread.sleep(30000);

        for (int i = 9; i <= 16; i++) {
            t[i].start();
        }

        for (int i = 9; i <= 16; i++) {
            t[i].join();
        }

        Client rc = new Client().withId("18").withType("peakLoad30s");
        Thread client = new Thread(rc);
        client.start();

        client.join();

        String serverReply = rc.getServerReply();
        JsonObject reply = new JsonObject();
        reply.addProperty("id", "18");
        reply.addProperty("status", "succeeded");
        reply.addProperty("response", "9");


        assertEquals(serverReply, reply.toString());
    }
}
