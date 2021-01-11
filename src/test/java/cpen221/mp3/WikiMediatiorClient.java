package cpen221.mp3;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cpen221.mp3.server.WikiMediatorServer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class WikiMediatiorClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Make a FibonacciClient and connect it to a server running on
     * hostname at the specified port.
     * @throws IOException if can't connect
     */
    public WikiMediatiorClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

    }

    /**
     */
    public void sendRequest(String id, String type, String query, int limit) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id",id);
        request.addProperty("type",type);
        request.addProperty("query",query);
        request.addProperty("limit",limit);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendRequest(String id, String type, String query, int limit, String timeout) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id",id);
        request.addProperty("type",type);
        request.addProperty("query",query);
        request.addProperty("limit",limit);
        request.addProperty("timeout", timeout);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendConnectedPages(String id, String type, String pageTitle, int hops) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", type);
        request.addProperty("pageTitle", pageTitle);
        request.addProperty("hops", hops);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendConnectedPages(String id, String type, String pageTitle, int hops, String timeout) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", type);
        request.addProperty("pageTitle", pageTitle);
        request.addProperty("hops", hops);
        request.addProperty("timeout", timeout);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendGetPage(String id, String type, String pageTitle) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", type);
        request.addProperty("pageTitle", pageTitle);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendGetPage(String id, String type, String pageTitle, String timeout) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", type);
        request.addProperty("pageTitle", pageTitle);
        request.addProperty("timeout", timeout);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendZeitgeist(String id, String type, int limit) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", type);
        request.addProperty("limit", limit);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendZeitgeist(String id, String type, int limit, String timeout) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", type);
        request.addProperty("limit", limit);
        request.addProperty("timeout", timeout);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendTrending(String id, String type, int limit) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", type);
        request.addProperty("limit", limit);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendTrending(String id, String type, int limit, String timeout) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", type);
        request.addProperty("limit", limit);
        request.addProperty("timeout", timeout);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    public void sendPeakLoad30s(String id, String type) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("type", type);

        String json = new Gson().toJson(request);
        out.println(json);
        out.flush();
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     * @return the requested Fibonacci number
     * @throws IOException if network or server failure
     */
    public String getReply() throws IOException {

        String reply = in.readLine();

        if (reply == null) {
            throw new IOException("connection terminated unexpectedly");
        }

        try {
            System.err.println("reply: " + reply);
            return reply;
        } catch (NumberFormatException nfe) {
            throw new IOException("misformatted reply: " + reply);
        }
    }

    /**
     * Closes the client's connection to the server.
     * This client is now "closed". Requires this is "open".
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }



    /**
     *
     */
    public static void main(String[] args) {
        try {
            WikiMediatiorClient client = new WikiMediatiorClient("localhost", WikiMediatorServer.WIKI_MEDIATOR_PORT);

            client.sendRequest("1", "simpleSearch","Barack Obama", 12);
            client.sendConnectedPages("1", "getConnectedPages", "Abu Dhabi Government Media Office(ADGMO)", 2);
            client.sendZeitgeist("1", "zeitgeist", 2);
            client.sendGetPage("1", "getPage", "Bernie Sanders");
            client.sendTrending("1", "trending", 2);
            client.sendPeakLoad30s("1", "peakLoad30s");


            // collect the replies
            //Check return type


            String replies = client.getReply();


            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
