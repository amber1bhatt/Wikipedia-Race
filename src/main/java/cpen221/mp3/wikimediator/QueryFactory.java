package cpen221.mp3.wikimediator;

import cpen221.mp3.QueryBaseListener;
import cpen221.mp3.QueryLexer;
import cpen221.mp3.QueryParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;

public class QueryFactory {
    public static Query parse(String string) {
        CharStream stream = CharStreams.fromString(string);
        QueryLexer lexer = new QueryLexer(stream);

        lexer.reportErrorsAsExceptions();
        TokenStream tokens = new CommonTokenStream(lexer);

        QueryParser parser =  new QueryParser(tokens);
        parser.reportErrorsAsExceptions();

        ParseTree tree = parser.query();

        System.err.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        QueryListener_QueryCreator listener = new QueryListener_QueryCreator();

        walker.walk(listener, tree);

        return listener.getQuery();
    }

    private static class QueryListener_QueryCreator extends QueryBaseListener {
        private List<Condition> conditions = new ArrayList<>();
        private String sorted = "";
        private String item = "";

        public void exitItem(QueryParser.ItemContext ctx) {
            if (ctx.AUTHOR() != null) {
                item = ctx.AUTHOR().getText();
            } else if (ctx.CATEGORY() != null) {
                item = ctx.CATEGORY().getText();
            } else if (ctx.PAGE() != null) {
                item = ctx.PAGE().getText();
            }
        }

        public void exitSimple_condition(QueryParser.Simple_conditionContext ctx) {
            String i = "";
            String query = "";

            if (ctx.AUTHOR() != null) {
                i = ctx.AUTHOR().getText();
            } else if (ctx.CATEGORY() != null) {
                i = ctx.CATEGORY().getText();
            } else if (ctx.TITLE() != null) {
                i = ctx.TITLE().getText();
            }

            if (ctx.STRING() != null) {
                query = ctx.STRING().getText();
            }

            Condition simpCond = new Condition(i, query);
            conditions.add(simpCond);
        }

        public void exitCondition(QueryParser.ConditionContext ctx) {
            if (ctx.OR() != null) {
                if (conditions.size() - 2 >= 0) {
                    Condition lastCondition2 = conditions.remove(conditions.size() - 2);
                    Condition cond2 = new Condition(lastCondition2.getItem(), lastCondition2.getQuery(), "or");
                    conditions.add(0, cond2);
                }
                Condition lastCondition1 = conditions.remove(conditions.size() - 1);
                Condition cond1 = new Condition(lastCondition1.getItem(), lastCondition1.getQuery(), "or");
                conditions.add(0, cond1);
            }
        }

        public void exitQuery(QueryParser.QueryContext ctx) {
            if(ctx.SORTED() != null) {
                sorted = ctx.SORTED().getText();
            }
        }

        public Query getQuery() {
            return new Query(item, conditions, sorted);
        }
    }

//    public void main(String[] args) {
//        Query que = parse("get page where category is 'Illinois State Senators'");
//    }
}
