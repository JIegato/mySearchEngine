package searchengine.utils;

import lombok.Getter;

@Getter
public class TextFragment {
    private String text;

    public TextFragment(String text) {
        this.text = text;
    }
}
