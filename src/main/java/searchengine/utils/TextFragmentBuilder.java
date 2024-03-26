package searchengine.utils;


import java.io.IOException;
import java.util.Set;

public class TextFragmentBuilder {
    public static String buildSnippet(Set<String> queries, String htmlContext) throws IOException {
        return getRightSnippet(queries, htmlContext).toString();
    }

    private static StringBuilder getRightSnippet(Set<String> queries, String htmlContext) {
        String point = ".";
        StringBuilder builder = new StringBuilder();
        for (String query : queries) {
            if (htmlContext.contains(" " + query + " ")) {
                int searchWord = htmlContext.toLowerCase().indexOf(query);
                int pointBefore = htmlContext.toLowerCase().lastIndexOf(point, searchWord);
                int pointAfter = htmlContext.toLowerCase().indexOf(point, searchWord);
                String string = htmlContext.toLowerCase().substring(pointBefore + 1, pointAfter);
                builder.append(string.replaceAll(query, "<b>" + query + "</b>") + "\n");
            }
        }
        if (builder.length() == 0) {
            for (String query : queries) {
                if (htmlContext.contains(query)) {
                    int searchWord = htmlContext.toLowerCase().indexOf(query);
                    int pointBefore = htmlContext.toLowerCase().lastIndexOf(point, searchWord);
                    int pointAfter = htmlContext.toLowerCase().indexOf(point, searchWord);
                    String string = htmlContext.toLowerCase().substring(pointBefore + 1, pointAfter);
                    builder.append(string.replaceAll(query, "<b>" + query + "</b>") + "\n");
                }
            }

        } return builder;
    }
}
