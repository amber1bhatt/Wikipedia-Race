package cpen221.mp3.server;

import com.google.gson.*;
import com.sun.jdi.InvalidTypeException;
import cpen221.mp3.wikimediator.InvalidQueryException;
import cpen221.mp3.wikimediator.WikiMediator;
import fastily.jwiki.core.Wiki;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiMediatorServer {

    /** Default port number where the server listens for connections. */
    public static final int WIKI_MEDIATOR_PORT = 4949;

    private ServerSocket serverSocket;
    private final Integer maxThreadsCount;
    private AtomicInteger currThreadCount;
    private final WikiMediator wm;
    private final Gson gson;

    //  Representation Invariants:
    //      - serverSocket cannot be null
    //
    //  Abstraction Function:
    //      Represents a server to process wiki mediator requests
    //
    // Thread Safety Argument:
    //      This class is thread safe because:
    //          - it utilizes a threadsafe class wikiMediator
    //          - WIKI_MEDIATOR_PORT is final
    //          - readers, writers, and data in handle() are treated as local objects
    //          - serve() is able to handle multiple clients

    private class simpleSearchRequest {
        final private String id;
        final private String query;
        final private String limit;
        private String timeout;

        simpleSearchRequest(String id, String query, String limit){
            this.id = id;
            this.query = query;
            this.limit = limit;
        }

        simpleSearchRequest(String id, String query, String limit, String Timeout) {
            this.timeout = Timeout;
            this.id = id;
            this.query = query;
            this.limit = limit;
        }

        @Override
        public String toString() {
            return "Simple Search Request [id=" + id + ", query=" + query + ", limit=" + limit + ", timeout=" + timeout +  "]";
        }
    }

    private class getPageRequest {
        final private String id;
        final private String pageTitle;
        private String timeout;

        public getPageRequest(String id, String pageTitle){
            this.id = id;
            this.pageTitle = pageTitle;
        }

        public getPageRequest(String id, String pageTitle, String timeout) {
            this.timeout = timeout;
            this.id = id;
            this.pageTitle = pageTitle;
        }

        @Override
        public String toString() {
            return "Get Page Request [id=" + id + ", pageTitle=" + pageTitle + ", timeout=" + timeout + "]";
        }
    }

    private class getConnectedPagesRequest {
        final private String id;
        final private String pageTitle;
        final private String hops;
        private String timeout;

        getConnectedPagesRequest(String id, String pageTitle, String hops){
            this.id = id;
            this.pageTitle = pageTitle;
            this.hops = hops;
        }

        public getConnectedPagesRequest(String id, String pageTitle, String hops, String timeout){
            this.id = id;
            this.pageTitle = pageTitle;
            this.hops = hops;
            this.timeout = timeout;
        }

        @Override
        public String toString() {
            return "Get Connected Pages Request [id=" + id + ", pageTitle=" + pageTitle + ", hops=" + hops + ", timeout=" + timeout + "]";
        }
    }

    private class zeitTrendRequest {
        final private String id;
        final private String limit;
        private String timeout;

        public zeitTrendRequest(String id, String limit){
            this.id = id;
            this.limit = limit;
        }

        public zeitTrendRequest(String id, String limit, String timeout){
            this.id = id;
            this.limit = limit;
            this.timeout = timeout;
        }


        @Override
        public String toString() {
            return "Zeitgeist or Trending Request [id=" + id + ", limit=" + limit + ", timeout=" + timeout +  "]";
        }
    }

    private class response {
        final private String id;
        final private String status;
        final private String response;

        public response(String id, boolean status, String response) {
            this.id = id;
            if (status) {
                this.status = "succeeded";
            } else {
                this.status = "failed";
            }
            this.response = response;
        }

        @Override
        public String toString() {
            return "response [id=" + id + ", status=" + status + ", response=" + response +  "]";
        }
    }

    /**
     * Start a server at a given port number, with the ability to process
     * up to n requests concurrently.
     *
     * @param port the port number to bind the server to
     *             port number, requires 0 <= port <= 65535
     * @param n the number of concurrent requests the server can handle
     */
    public WikiMediatorServer(int port, int n) throws IOException {
        maxThreadsCount = n;
        currThreadCount = new AtomicInteger(0);
        serverSocket = new ServerSocket(port);
        wm = new WikiMediator();
        gson = new GsonBuilder().setLenient().disableHtmlEscaping().create();
    }

    /**
     * Run the server, listening for connections and handling them.
     *
     * @throws IOException
     *             if the main server socket is broken
     */
    public void serve() throws IOException {
        while (currThreadCount.incrementAndGet() <= maxThreadsCount) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            // create a new thread to handle that client
            Thread handler = new Thread(new Runnable() {
                public void run() {
                    try {
                        try {
                            handle(socket);
                        } finally {
                            socket.close();
                            currThreadCount.decrementAndGet();
                        }
                    } catch (IOException | InvalidQueryException ioe) {
                        // this exception wouldn't terminate serve(),
                        // since we're now on a different thread, but
                        // we still need to handle it
                        ioe.printStackTrace();
                    }
                }
            });
            // start the thread
            handler.start();
        }

    }

     /**
     * Handle one client connection. Returns when client disconnects.
     *
     * @throws IOException
     *             if connection encounters an error
     * @throws InvalidQueryException
      *             if query has unexpected type
     */
    public void handle(Socket socket) throws IOException, InvalidQueryException {
        System.err.println("client connected");

        // get the socket's input stream, and wrap converters around it
        // that convert it from a byte stream to a character stream,
        // and that buffer it so that we can read a line at a time
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));


        // similarly, wrap character=>bytestream converter around the
        // socket output stream, and wrap a PrintWriter around that so
        // that we have more convenient ways to write Java primitive
        // types to it.
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        ExecutorService exec = Executors.newCachedThreadPool();


            for (String line = in.readLine(); line != null; line = in.readLine()) {
                System.err.println("request: " + line);
                JsonObject jObj = gson.fromJson(line, JsonObject.class);
                Future<response> future = exec.submit(() -> {

                    String res = "";
                    Boolean status = false;

                    switch (jObj.get("type").getAsString()) {
                        case "simpleSearch":
                            simpleSearchRequest ssrequest;
                            ssrequest = gson.fromJson(jObj, simpleSearchRequest.class);
                            try {
                                List<String> searchList = wm.simpleSearch(ssrequest.query, Integer.parseInt(ssrequest.limit));
                                res = searchList.toString();
                                status = true;
                            } catch (Exception e) {
                                res = e.toString();
                            }
                            break;

                        case "getPage":
                            getPageRequest gprequest = gson.fromJson(jObj, getPageRequest.class);
                            try {
                                res = wm.getPage(gprequest.pageTitle);
                                status = true;
                            } catch (Exception e) {
                                res = e.toString();
                            }
                            break;

                        case "getConnectedPages":
                            getConnectedPagesRequest gcrequest = gson.fromJson(jObj, getConnectedPagesRequest.class);
                            try {
                                List<String> searchList = wm.getConnectedPages(gcrequest.pageTitle, Integer.parseInt(gcrequest.hops));
                                res = searchList.toString();
                                status = true;
                            } catch (Exception e) {
                                res = e.toString();
                            }
                            break;

                        case "trending":
                            zeitTrendRequest trequest = gson.fromJson(jObj, zeitTrendRequest.class);
                            try {
                                List<String> searchList = wm.trending(Integer.parseInt(trequest.limit));
                                res = searchList.toString();
                                status = true;
                            } catch (Exception e) {
                                res = e.toString();
                            }
                            break;

                        case "zeitgeist":
                            zeitTrendRequest zrequest = gson.fromJson(jObj, zeitTrendRequest.class);
                            try {
                                List<String> searchList = wm.zeitgeist(Integer.parseInt(zrequest.limit));
                                res = searchList.toString();
                                status = true;
                            } catch (Exception e) {
                                res = e.toString();
                            }
                            break;

                        case "peakLoad30s":
                            try {
                                res = String.valueOf(wm.peakLoad30s());
                                status = true;
                            } catch (Exception e) {
                                res = e.toString();
                            }
                            break;
                    }
                    response resp = new response(jObj.get("id").getAsString(), status, res);

                    return resp;
                });

                String reply = "";
                try {
                    if (jObj.get("timeout") != null) {
                        response res = future.get(jObj.get("timeout").getAsLong(), TimeUnit.SECONDS);
                        reply = gson.toJson(res);
                    } else {
                        response res = future.get();
                        reply = gson.toJson(res);
                    }
                    System.err.println("reply: " + reply);
                    out.println(reply);
                    out.flush();
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    response res = new response(jObj.get("id").getAsString(), false, "Operation timed out");
                    reply = gson.toJson(res);
                    future.cancel(true);
                    e.printStackTrace();
                    System.err.println("reply: " + reply);
                    out.println(reply);
                    out.flush();
                }
            }

            out.close();
            in.close();

    }

    /**
     * Start a WikiMediatorServer running on the default port.
     */
    public static void main(String[] args) {
        try {
            WikiMediatorServer server = new WikiMediatorServer(WIKI_MEDIATOR_PORT, 1);
            server.serve();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
