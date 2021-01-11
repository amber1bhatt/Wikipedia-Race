package cpen221.mp3.wikimediator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.antlr.v4.runtime.misc.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Pair<String, String>> requests = new ArrayList<>();
    private AtomicInteger maxRequestCount = new AtomicInteger();
    private AtomicInteger requestCount = new AtomicInteger();

    public void addRequest(Pair<String, String> request) {
        requests.add(request);
    }

    public List<Pair<String, String>> getRequests() {
        return new ArrayList<>(requests);
    }

    public void setCount(int maxVal, int val) {
        maxRequestCount.compareAndSet(maxRequestCount.get(), maxVal);
        requestCount.compareAndSet(requestCount.get(), val);
    }

    @Override
    public String toString() {
        JsonObject obj = new JsonObject();

        JsonArray arr = new JsonArray();
        for (Pair<String, String> req : requests) {
            arr.add(req.a.toString() + "    " + req.b.toString());
        }

        obj.add("Requests", arr);
        obj.addProperty("requestCount", requestCount.toString());
        obj.addProperty("maxRequestCount", maxRequestCount.toString());

        //return "hello";

        return new StringBuffer(obj.toString()).toString();
    }
}
