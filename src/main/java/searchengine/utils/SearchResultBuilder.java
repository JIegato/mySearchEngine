package searchengine.utils;

import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResulItem;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.SearchEngineRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SearchResultBuilder {
    private final int pagePercentageLimit = 500;

    public SearchResponse getSearchResult(String siteUrl, String query) throws IOException {
        Site site = getSite(siteUrl);
        List<Lemma> lemmaFullList = getLemmaList(site, query);
        long countPages = getCountPages(site);
        List<Lemma> lemmaInfrequentList = getInfrequentLemmas(lemmaFullList, countPages);
        List<Page> pages = getCommonPagesForLemmas(lemmaInfrequentList);
        if (pages.isEmpty()) {
            SearchResponse response = new SearchResponse();
            response.setCount(0);
            List<SearchResulItem> resultItems = new ArrayList<>();
            response.setData(resultItems);
            return response;
        }
        RelevancyBuilder relevancyBuilder = new RelevancyBuilder(pages, lemmaInfrequentList);
        List<Map.Entry<Page, Float>> relevancyPages = relevancyBuilder.getRelevancyList();

        Set<String> lemmaSet = Lemmatizator.init().getAllForm(lemmaInfrequentList);
        SearchResponse response = new SearchResponse();
        response.setCount(pages.size());
        List<SearchResulItem> resultItems = new ArrayList<>();
        relevancyPages.forEach(pageEntry -> {
            Page page = pageEntry.getKey();
            SearchResulItem item = new SearchResulItem();
            item.setSite(page.getSite().getUrl());
            item.setSiteName(page.getSite().getName());
            item.setUri(page.getPath());
            item.setTitle(page.getTitle());
            try {
                item.setSnippet(TextFragmentBuilder.buildSnippet(lemmaSet, page.getContent()));
            } catch (IOException e) {
                item.setSnippet("");
            }
            item.setRelevance(pageEntry.getValue());
            resultItems.add(item);
        });

        response.setData(resultItems);
        return response;
    }

    private List<Page> getCommonPagesForLemmas(List<Lemma> lemmas) {
        if (lemmas.isEmpty()) return new ArrayList<Page>();
        List<Page> pages = lemmas.get(0).getIndexes().stream().map(Index::getPage).collect(
                ArrayList::new,
                List::add,
                List::addAll
        );
        for (int i = 1; i < lemmas.size(); i++) {
            Set<Page> pagesByLemma = lemmas.get(i).getIndexes().stream().map(Index::getPage).collect(Collectors.toSet());
            Iterator<Page> pageIterator = pages.iterator();
            while (pageIterator.hasNext()) {
                if (!pagesByLemma.contains(pageIterator.next())) {
                    pageIterator.remove();
                }
            }
        }
        return pages;
    }

    private List<Lemma> getInfrequentLemmas(List<Lemma> lemmaList, long countPages) {
        return lemmaList.stream()
                .filter(lemma -> {
                    long count = lemma.getIndexes().size();
                    return (int) (100 * ((double) count) / ((double) countPages)) <= pagePercentageLimit;
                })
                .sorted(new LemmaComparator())
                .collect(Collectors.toList());
    }

    private Site getSite(String siteUrl) {
        Optional<Site> siteOptional = SearchEngineRepository.siteRepository.findByUrl(siteUrl);
        return siteOptional.orElse(null);
    }

    private Set<String> getWordsFromText(String text) throws IOException {
        return Lemmatizator.init().getLemmaSet(text);
    }

    private long getCountPages(Site site) {
        if (site == null) {
            return SearchEngineRepository.pageRepository.count();
        } else {
            return SearchEngineRepository.pageRepository.countBySite(site);
        }
    }

    private List<Lemma> getLemmaList(Site site, String query) throws IOException {
        Set<String> words = getWordsFromText(query);
        if (site == null) {
            return SearchEngineRepository.lemmaRepository.findAll().stream()
                    .filter(lemma -> words.contains(lemma.getLemma()))
                    .collect(Collectors.toList());
        } else {
            return site.getLemmas().stream()
                    .filter(lemma -> words.contains(lemma.getLemma()))
                    .collect(Collectors.toList());
        }
    }

    private static class LemmaComparator implements Comparator<Lemma> {

        @Override
        public int compare(Lemma l1, Lemma l2) {
            if (l1.getFrequency() < l2.getFrequency()) {
                return -1;
            }
            if (l1.getFrequency() > l2.getFrequency()) {
                return 1;
            }
            return 0;
        }
    }
}
