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
        String[] words = snippet.replaceAll("([^а-яА-Я\\s])", " ").trim().split("\\s+");
        Set<String> setToReplace = new HashSet<>();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            List<String> forms = Lemmatizator.init().getNormalForms(word.toLowerCase(Locale.ROOT));
            boolean isFound = false;
            for (String form : forms) {
                if (searchWords.contains(form)) {
                    setToReplace.add(word);
                    isFound = true;
                    break;
                }
            }
            if (isFound) break;
        }
        for (String replString : setToReplace) {
            snippet = snippet.replaceAll(replString, "<b>" + replString + "</b>");
        }
        return snippet;
    }

    private static String getRightSnippet(String query, String htmlContext) throws IOException {
        String encodedHtml = StringEscapeUtils.unescapeHtml4(htmlContext);
        Document document = Jsoup.parse(encodedHtml);

        Set<String> searchWords = Lemmatizator.init().getLemmaSet(query);

        Elements elements = document.select("p");
        Map<TextFragment, Integer> fragmentMap = new HashMap<>();
        elements.forEach(element -> fragmentMap.put(new TextFragment(element.text()), 0));
        if (fragmentMap.size() == 0) {
            return "";
        }

        List<Map.Entry<TextFragment, Integer>> entryList = new ArrayList<>(fragmentMap.entrySet());
        for (Map.Entry<TextFragment, Integer> entry : entryList) {
            Set<String> wordsFromFragment = Lemmatizator.init().getLemmaSet(entry.getKey().getText());
            if (wordsFromFragment.size() == 0) continue;
            for (String word : searchWords) {
                if (wordsFromFragment.contains(word)) {
                    entry.setValue(entry.getValue() + 1);
                }
            }
        }
        Collections.sort(entryList, Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(entryList);
        return entryList.get(0).getKey().getText();
    }
}
