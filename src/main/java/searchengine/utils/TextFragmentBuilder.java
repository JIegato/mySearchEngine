package searchengine.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class TextFragmentBuilder {
    public static String buildSnippet(String query, String htmlContext) throws IOException {
        String snippet = getRightSnippet(query, htmlContext);
        return setBold(query, snippet);
    }

    private static String setBold(String query, String text) throws IOException {
        String snippet = text;
        Set<String> searchWords = Lemmatizator.init().getLemmaSet(query);

        String[] words = snippet.split("\\s+");
        Set<String> setToReplace = new HashSet<>();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (searchWords.contains(word.toLowerCase(Locale.ROOT))) {
                setToReplace.add(word);
                break;
            }
        }

        for (String replString : setToReplace) {
            snippet = snippet.replaceAll(replString, "<b>" + replString + "</b>");
        }

        return snippet;
    }

    private static String getRightSnippet(String query, String htmlContext) throws IOException {
        String decodedHtml = StringEscapeUtils.unescapeHtml4(htmlContext);
        Document document = Jsoup.parse(decodedHtml);
        String elements = document.text();
        String point = ".";
        int searchWord = elements.indexOf(query);
        int pointBefore = elements.lastIndexOf(point, searchWord);
        int pointAfter = elements.indexOf(point, searchWord);
        String snippet = elements.substring(pointBefore+1, pointAfter);
        return snippet;
    }


}

