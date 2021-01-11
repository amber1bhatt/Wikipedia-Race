package cpen221.mp3;

import cpen221.mp3.wikimediator.InvalidQueryException;
import cpen221.mp3.wikimediator.WikiMediator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fastily.jwiki.core.Wiki;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WikiMediatorTests {

    @Test
    public void simpleSearchTest1() {
        String query = "Computer Engineering";
        int limit = 5;

        WikiMediator wm = new WikiMediator();

        List<String> list = new ArrayList<String>();
        list.add("Computer Science and Engineering");
        list.add("Computer engineering");
        list.add("Computer science");
        list.add("Computer-aided engineering");
        list.add("Outline of computer engineering");

        List<String> listRes =  wm.simpleSearch(query, limit);

        assertEquals(list, listRes);
    }

    @Test
    public void getPageTest1() {
        String query = "Scarburgh";
        int limit = 5;

        WikiMediator wm = new WikiMediator();

        String text = "'''Scarburgh''' is a surname. Notable people with the name include:\n" +
                "*[[George Scarburgh]]\n" +
                "*[[Charles Scarburgh]], English mathematician\n" +
                "*[[John Scarburgh]], English Member of Parliament\n" +
                "\n" +
                "==See also==\n" +
                "*[[Scarborough (disambiguation)]]\n" +
                "\n" +
                "{{surname}}";

        String res =  wm.getPage(query);

        assertEquals(text, res);
    }

    @Test
    public void getConnectedPageTest1() {
        String query = "Scarburgh";
        int hops = 1;

        WikiMediator wm = new WikiMediator();

        List<String> list = new ArrayList<>();
        list.add("Wikipedia:Manual of Style/Linking");
        list.add("Given name");
        list.add("John Scarburgh");
        list.add("Scarborough (disambiguation)");
        list.add("Charles Scarburgh");
        list.add("Scarburgh");
        list.add("Surname");
        list.add("Talk:Scarburgh");
        list.add("George Scarburgh");

        List<String> listRes =  wm.getConnectedPages(query, hops);

        assertEquals(list, listRes);
    }

//    @Test
//    public void simpleSearchTest2() {
//        String query = "Computer Engineering";
//        int limit = 5;
//
//        WikiMediator wm = new WikiMediator();
//
//        List<String> list = new ArrayList<String>();
//        list.add("Computer engineering");
//        list.add("Computer Science and Engineering");
//        list.add("Computer-aided engineering");
//        list.add("Outline of computer engineering");
//        list.add("Computer science");
//
//        List<String> listRes =  wm.simpleSearch(query, limit);
//        List<String> listRes1 =  wm.simpleSearch(query, limit);
//
//        assertEquals(listRes1, listRes);
//        assertEquals(listRes1, listRes);
//    }

    @Test
    public void zeitgeistTest1() throws InterruptedException {
        String query = "Computer Engineering";
        int limit = 5;

        WikiMediator wm = new WikiMediator();

        List<String> list = new ArrayList<String>();
        list.add("Computer Engineering");
        list.add("Computer");

        wm.simpleSearch(query, limit);
        wm.simpleSearch(query, limit);
        wm.simpleSearch(query, limit);
        wm.simpleSearch(query, limit);
        wm.getPage(query);
        wm.getPage(query);
        wm.getPage(query);
        wm.getConnectedPages("value", 1);
        wm.getPage("value");
        wm.getPage("value");
        wm.getConnectedPages("value", 1);
        wm.getConnectedPages(query, 1);
        wm.getConnectedPages(query, 1);
        wm.simpleSearch("Number", 1);
        wm.simpleSearch("Computer", 1);
        wm.simpleSearch("Computer", 1);
        wm.simpleSearch("Computer", 1);

        List<String> zeitList = wm.zeitgeist(2);

        assertEquals(list, zeitList);
    }

    @Test
    public void trendingTest1() throws InterruptedException {
        String query1 = "Computer Engineering";
        String query2 = "Orange";
        String query3 = "Holy Cow";
        int limit = 3;

        WikiMediator wm = new WikiMediator();

        List<String> list = new ArrayList<String>();
        list.add(query3);

        wm.simpleSearch(query1, limit);
        wm.getConnectedPages(query1, 1);
        wm.getConnectedPages(query1, 1);
        wm.simpleSearch(query1, limit);
        wm.getPage(query2);
        wm.getPage(query2);
        wm.getPage(query2);
        wm.getConnectedPages(query3, 1);

        Thread.sleep(30000);

        wm.simpleSearch(query3, limit);
        wm.simpleSearch(query3, limit);
        wm.simpleSearch(query3, limit);
        wm.simpleSearch(query3, limit);
        wm.simpleSearch(query3, limit);
        wm.simpleSearch(query3, limit);

        List<String> trending = wm.trending(limit);

        assertEquals(trending, list);
    }

    @Test
    public void peakLoadTest1() throws InterruptedException {
        String query = "Computer Engineering";
        int limit = 5;

        WikiMediator wm = new WikiMediator();

        wm.simpleSearch(query, limit);
        wm.getConnectedPages(query, 1);
        wm.simpleSearch(query, limit);
        wm.getPage(query);

        Thread.sleep(30000);

        wm.simpleSearch(query, limit);
        wm.getConnectedPages(query, 1);
        wm.zeitgeist(limit);
        wm.getConnectedPages(query, 1);
        wm.getPage(query);

        Thread.sleep(30000);

        wm.trending(limit);
        wm.simpleSearch(query, limit);
        wm.zeitgeist(limit);

        Thread.sleep(30000);

        int count = wm.peakLoad30s();

        assertEquals(5, count);
    }

    @Test
    public void getPathTest1() {
        String query = "Scarburgh";

        WikiMediator wm = new WikiMediator();

        List<String> list = new ArrayList<>();
        list.add("Scarburgh");
        list.add("Wikipedia:Manual of Style/Linking");
        list.add("MOS:DL");

        List<String> listRes =  wm.getPath(query, "MOS:DL");

        assertEquals(list, listRes);
    }

    @Test
    public void getPathTest2() {

        String query = "Lunari";

        WikiMediator wm = new WikiMediator();

        List<String> list = new ArrayList<>();
        list.add("Lunari");
        list.add("Luigi Lunari");

        List<String> listRes = wm.getPath(query, "Luigi Lunari");

        assertEquals(list, listRes);
    }

    @Test
    public void getPathTest3() {

        String startPage = "UBC";
        String endPage = "Nuclear medicine";

        WikiMediator wm = new WikiMediator();

        List<String> listRes = wm.getPath(startPage, endPage);

        List<String> list = new ArrayList<>();
        list.add("UBC");
        list.add("University of British Columbia");
        list.add("Nuclear physics");
        list.add("Nuclear medicine");

        assertEquals(list, listRes);
    }

    @Test
    public void getPathSameStartStopTest() {

        String startPage = "UBC";
        String endPage = "UBC";

        WikiMediator wm = new WikiMediator();

        List<String> listRes = wm.getPath(startPage, endPage);

        List<String> list = new ArrayList<>();
        list.add("UBC");

        assertEquals(list, listRes);
    }

    @Test
    public void getPathOvertimeTest() {

        String startPage = "Barack Obama";
        String endPage = "Metallurgy";

        WikiMediator wm = new WikiMediator();

        List<String> listRes = wm.getPath(startPage, endPage);

        List<String> list = new ArrayList<>();

        assertEquals(list, listRes);
    }

    @Test
    public void getPathOvertimeTest2() {

        String startPage = "Pterodactylus";
        String endPage = "Johnson & Johnson";

        WikiMediator wm = new WikiMediator();

        List<String> listRes = wm.getPath(startPage, endPage);

        List<String> list = new ArrayList<>();

        assertEquals(list, listRes);
    }

    @Test
    public void excuteQueryTest1() throws InvalidQueryException {
        WikiMediator wm = new WikiMediator();

        List<String> resList = wm.excuteQuery("get author where category is 'Illinois state senators'");

        List<String> list = new ArrayList<>();
    }

    @Test
    public void excuteQueryTest2() throws InvalidQueryException {
        WikiMediator wm = new WikiMediator();

        List<String> resList = wm.excuteQuery("get category where title is 'Duloxetine'");

        List<String> list = new ArrayList<>();
    }

    @Test
    public void excuteQueryTest3() throws InvalidQueryException {
        WikiMediator wm = new WikiMediator();

        List<String> resList = wm.excuteQuery("get category where (title is 'Duloxetine' and (title is 'Barack Obama' or author is 'Maborland'))");

        List<String> list = new ArrayList<>();
        list.add("Category:All articles to be expanded");
        list.add("Category:Articles to be expanded from October 2009");
        list.add("Category:Articles using small message boxes");
        list.add("Category:Asian-American culture");
    }
}

