package searchengine.utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class Lemmatizator {
    private final LuceneMorphology luceneMorphology;
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ", "ПРЕДК"};

    private Lemmatizator(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }

    public static Lemmatizator init() throws IOException {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        return new Lemmatizator(morphology);
    }

    public Map<String, Integer> getLemmasNumberOfReferences(String text) {
        String[] words = textToWordsRU(text);
        Map<String, Integer> result = new HashMap<>();
        for (String word : words) {
            if (word.isBlank()) continue;
            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) continue;

            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (!normalForms.isEmpty()) {
                String normalForm = normalForms.get(0);
                result.put(normalForm, result.getOrDefault(normalForm, 0) + 1);
            }
        }
        return result;
    }

    public Set<String> getLemmaSet(String text) {
        String[] textArray = textToWordsRU(text);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : textArray) {
            if (!word.isEmpty() && isCorrectWordForm(word)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (!anyWordBaseBelongToParticle(wordBaseForms)) {
                    lemmaSet.addAll(luceneMorphology.getNormalForms(word));
                }
            }
        }
        return lemmaSet;
    }

    public List<String> getNormalForms(String word) {
        if (!word.isEmpty() && isCorrectWordForm(word)) {
            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (!anyWordBaseBelongToParticle(wordBaseForms)) {
                return luceneMorphology.getNormalForms(word);
            }
        }
        return new ArrayList<>();
    }

    private boolean isCorrectWordForm(String word) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        return wordInfo.stream().noneMatch(morphInfo -> morphInfo.matches(WORD_TYPE_REGEX));
    }

    private String[] textToWordsRU(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        return Arrays.stream(particlesNames).anyMatch(property -> wordBase.toUpperCase().contains(property));
    }
}
