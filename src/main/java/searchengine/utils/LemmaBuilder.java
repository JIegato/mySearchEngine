package searchengine.utils;

import lombok.Setter;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.SearchEngineRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Setter
public class LemmaBuilder {
    private Site site;
    private Page page;
    private Map<String, Integer> lemmas = new HashMap<>();

    public static void buildLemmas(Site site, Page page) throws IOException {
        LemmaBuilder builder = new LemmaBuilder();
        builder.setSite(site);
        builder.setPage(page);
        builder.build();
    }

    private void build() throws IOException {
        lemmas = Lemmatizator.init().getLemmasNumberOfReferences(page.getContent());
        for (Map.Entry<String, Integer> entry: lemmas.entrySet()){
            if (!SiteIndexBuilder.isStarted()) return;
            Lemma lemma = createIfNotAvailable(entry.getKey());
            IndexBuilder.buildIndex(page, lemma, entry.getValue());
        }
    }

    private Lemma createIfNotAvailable(String normalForm) {
        Optional lemmaOptional = SearchEngineRepository.lemmaRepository.findBySiteAndLemma(site, normalForm);
        if (!lemmaOptional.isEmpty()){
            Lemma lemma = (Lemma) lemmaOptional.get();
            lemma.setFrequency(lemma.getFrequency()+1);
            SearchEngineRepository.lemmaRepository.save(lemma);
            return lemma;
        }
        Lemma lemma = new Lemma();
        lemma.setSite(site);
        lemma.setLemma(normalForm);
        lemma.setFrequency(1);
        SearchEngineRepository.lemmaRepository.save(lemma);
        return lemma;
    }

}
