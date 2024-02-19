package searchengine.utils;

import lombok.Setter;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.SearchEngineRepository;

import java.util.Optional;

@Setter
public class IndexBuilder {
    private Page page;
    private Lemma lemma;

    public static void buildIndex(Page page, Lemma lemma, int rank){
        IndexBuilder builder = new IndexBuilder();
        builder.setPage(page);
        builder.setLemma(lemma);
        synchronized (IndexBuilder.class){
            builder.createIfNotAvailable(rank);
        }
    }

    private void createIfNotAvailable(int rank){
        Optional indexOptional = SearchEngineRepository.indexRepository.findByPageAndLemma(page, lemma);
        if (!indexOptional.isEmpty()){
            Index index = (Index) indexOptional.get();
            index.setRank((float) rank);
            SearchEngineRepository.indexRepository.save(index);

        }
        Index index = new Index();
        index.setPage(page);
        index.setLemma(lemma);
        index.setRank((float) rank);
        SearchEngineRepository.indexRepository.save(index);
    }
}
