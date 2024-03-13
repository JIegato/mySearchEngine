package searchengine.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class TextFragmentBuilder {
    public static String buildSnippet(String query, String htmlContext) throws IOException {
        String snippet = getRightSnippet(query, htmlContext);
        return setBold(query, snippet);
    }

    private static String setBold(String query, String text)  {
        return text.toLowerCase().replaceAll(query, "<b>" + query + "</b>");
    }

    private static String getRightSnippet(String query, String htmlContext)  {
        String decodedHtml = StringEscapeUtils.unescapeHtml4(htmlContext);
        Document document = Jsoup.parse(decodedHtml);
        String elements = document.text();
        String point = ".";
        int searchWord = elements.indexOf(query);
        int pointBefore = elements.lastIndexOf(point, searchWord);
        int pointAfter = elements.indexOf(point, searchWord);
        return elements.substring(pointBefore+1, pointAfter);
    }


}

