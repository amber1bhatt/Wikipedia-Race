package cpen221.mp3.wikimediator;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private final String item;
    private final List<Condition> cond;
    private final String sorted;

    public Query(String item, List<Condition> cond, String sorted) {
        this.item = item;
        this.cond = new ArrayList<>(cond);
        this.sorted = sorted;
    }

    public ArrayList<Condition> getConditions() {
        return new ArrayList<>(cond);
    }

    public String getItem() {
        return item;
    }

    public String getSorted() {
        return sorted;
    }
}
