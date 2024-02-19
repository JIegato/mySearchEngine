package searchengine.utils;

import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.*;

import java.util.stream.Collectors;

public class RelevancyBuilder {
    private final List<Page> pages;
    private final List<Lemma> lemmas;
    private Map<Page, Map<Lemma,Float>> relevanceMap = new HashMap<>();
    private Map<Page, Float> absoluteRelevance = new HashMap<>();
    private Map<Page, Float> relativeRelevance = new HashMap<>();
    private float maxAbsoluteRelevance = 0;

    public RelevancyBuilder(List<Page> pages, List<Lemma> lemmas) {
        this.pages = pages;
        this.lemmas = lemmas;
    }

    public List<Map.Entry<Page, Float>> getRelevancyList(){
        prepareRelevancyMap();
        calcAbsoluteRelevance();
        calcRelativeRelevance();
        List<Map.Entry<Page, Float>> entries = new ArrayList<>(relativeRelevance.entrySet());
        Collections.sort(entries, Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(entries);
        return entries;
    }

    private void prepareRelevancyMap(){
        pages.forEach(page -> {
            List<Index> indexList = page.getIndexes()
                    .stream()
                    .filter(index -> lemmas.contains(index.getLemma()))
                    .collect(Collectors.toList());

            addIndex(indexList);
        });

    }

    private void addIndex(List<Index> indexList){
        for (Index index : indexList){
            if (relevanceMap.get(index.getPage()) == null){
                Map<Lemma, Float> lemmaMap = new HashMap<>();
                lemmaMap.put(index.getLemma(), index.getRank());
                relevanceMap.put(index.getPage(), lemmaMap);
                continue;
            }
            Map<Lemma, Float> lemmaMap = relevanceMap.get(index.getPage());
            lemmaMap.put(index.getLemma(), index.getRank());
        }

    }

    private void calcAbsoluteRelevance(){
        for (Map.Entry<Page, Map<Lemma, Float>> entry: relevanceMap.entrySet()){
            Float abs = Float.valueOf(0);
            for (Map.Entry<Lemma, Float> entryLemmas : entry.getValue().entrySet()){
                abs += entryLemmas.getValue();
            }
            absoluteRelevance.put(entry.getKey(), abs);
            if (abs > maxAbsoluteRelevance){
                maxAbsoluteRelevance = abs;
            }
        }
    }

    private void calcRelativeRelevance() {
        for (Map.Entry<Page, Float> entry : absoluteRelevance.entrySet()){
            Float relative = entry.getValue() / maxAbsoluteRelevance;
            relativeRelevance.put(entry.getKey(), relative);
        }
    }
}
