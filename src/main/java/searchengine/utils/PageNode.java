package searchengine.utils;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class PageNode {
    private SiteGraph siteGraph;
    private Document document;

    public static PageNode init(SiteGraph siteGraph, String url) throws IOException, InterruptedException {
        Thread.sleep(150);
        PageNode node = new PageNode();
        node.setSiteGraph(siteGraph);
        Connection.Response response = Jsoup.connect(url)
                .userAgent(RandomUserAgent.getRandomUserAgent())
                .referrer("http://www.google.com")
                .execute();
        Document document = Jsoup.parse(response.body());
        node.setDocument(document);
        return node;
    }

    public HashSet<String> getChildrenPageUrl(){
        HashSet<String> links = new HashSet<>();
        Elements elementsWhitShortRoute = document.select("a[href~=^\\/[a-z0-9-_/]+\\/$]");
        elementsWhitShortRoute.forEach(e -> {
            String childrenPageUrl = e.attr("href");
            String url = siteGraph.getSite().getUrl() + childrenPageUrl;
            if (!siteGraph.isExistVertex(url)){
                links.add(url);
                siteGraph.addVertex(url);
            }
        });
        return links;
    }

    public String getTitle(){
        return document.title();
    }

    public String getContent(){
        return clearHtml(document.outerHtml());
    }

    private String clearHtml(String text) {
        Pattern pattern = Pattern.compile("[^а-яА-Я\\s]+", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        String result = matcher.replaceAll(" ");
        return clearSpaces(result);
    }

    private String clearSpaces(String text){
        Pattern pattern = Pattern.compile("[\\s]+", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        String result = matcher.replaceAll(" ");
        return result;
    }

    public String getEscapeHtmlContent() {
        String result = StringEscapeUtils.escapeHtml4(document.outerHtml());
        return result;
    }
}
