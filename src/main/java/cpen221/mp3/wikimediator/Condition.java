package cpen221.mp3.wikimediator;

public class Condition {
    private String operator = "and";
    private final String item;
    private final String query;

    public Condition(String item, String query) {
        this.item = item;
        this.query = query;
    }

    public Condition(String item, String query, String operator) {
        this.item = item;
        this.query = query;
        this.operator = operator;
    }

    public String getItem() {
        return item;
    }

    public String getQuery() {
        return query;
    }

    public String getOperator() {
        return operator;
    }
}
