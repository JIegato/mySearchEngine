package searchengine.utils;

import searchengine.model.Lemma;

import java.io.IOException;
import java.util.List;

public class TextFragmentBuilder {
    public static String buildSnippet(List<Lemma> queries, String htmlContext) throws IOException {
        return getRightSnippet(queries, htmlContext).toString();
    }

    private static StringBuilder getRightSnippet(List<Lemma> queries , String htmlContext) {
        String point = ".";
        StringBuilder builder = new StringBuilder();
        for (Lemma query: queries) {
            String word = query.getLemma();
            int searchWord = htmlContext.toLowerCase().indexOf(word);
            int pointBefore = htmlContext.toLowerCase().lastIndexOf(point, searchWord);
            int pointAfter = htmlContext.toLowerCase().indexOf(point, searchWord);
            String string = htmlContext.toLowerCase().substring(pointBefore + 1, pointAfter);
            builder.append(string.replaceAll(word, "<b>" + word + "</b>") + "\n");
        }
        return builder;
    }
}

