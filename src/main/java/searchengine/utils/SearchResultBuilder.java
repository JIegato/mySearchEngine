package searchengine.utils;

import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResulItem;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.SearchEngineRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SearchResultBuilder {
    private final int PAGE_PERCENTAGE_LIMIT = 40;


    public SearchResponse getSearchResult(String siteUrl, String query) throws IOException {
        Site site = getSite(siteUrl);
        List<Lemma> lemmaFullList = getLemmaList(site, query);
        long countPages = getCountPages(site);
        List<Lemma> lemmaInfrequentList = getInfrequentLemmas(lemmaFullList, countPages);
        List<Page> pages = getCommonPagesForLemmas(lemmaInfrequentList);
        if (pages.isEmpty()) return new SearchResponse();
        RelevancyBuilder relevancyBuilder = new RelevancyBuilder(pages, lemmaInfrequentList);
        List<Map.Entry<Page, Float>> relevancyPages = relevancyBuilder.getRelevancyList();

        SearchResponse response = new SearchResponse();
        response.setCount(pages.size());
        List<SearchResulItem> resulItems = new ArrayList<>();
        relevancyPages.forEach(pageEntry -> {
            Page page = pageEntry.getKey();
            SearchResulItem item = new SearchResulItem();
            item.setSite(page.getSite().getUrl());
            item.setSiteName(page.getSite().getName());
            item.setUri(page.getPath());
            item.setTitle(page.getTitle());
            try {
                item.setSnippet(TextFragmentBuilder.buildSnippet(query, page.getHtmlContent()));
            } catch (IOException e) {
                item.setSnippet("");
            }
            item.setRelevance(pageEntry.getValue());
            resulItems.add(item);
        });

        response.setData(resulItems);
        return response;
    }

    private List<Page> getCommonPagesForLemmas(List<Lemma> lemmas){
        if (lemmas.isEmpty()) return new ArrayList<Page>();
        List<Page> pages = lemmas.get(0).getIndexes().stream().collect(
                ()->new ArrayList<Page>(),
                (l, i) ->l.add(i.getPage()),
                (l1, l2) -> l1.addAll(l2)
        );
        for (int i = 1; i < lemmas.size(); i++) {
            ArrayList<Page> pagesByLemma = lemmas.get(i).getIndexes().stream().collect(
                    ()->new ArrayList<Page>(),
                    (list, index) ->list.add(index.getPage()),
                    (pages1, pages2) -> pages1.addAll(pages2)
            );
            Iterator<Page> pageIterator = pages.iterator();
            while (pageIterator.hasNext()){
                if (!pagesByLemma.contains(pageIterator.next())){
                    pageIterator.remove();
                }
            }
        }
        return pages;
    }

    private List<Lemma> getInfrequentLemmas(List<Lemma> lemmaList, long countPages) {
        List<Lemma> result = new ArrayList<>();
        for (Lemma lemma : lemmaList){
            long count = lemma.getIndexes().size();
            if ( (int) (100*((double)count)/((double)countPages))>PAGE_PERCENTAGE_LIMIT ){
                continue;
            }
            result.add(lemma);
        }
        return result.stream()
                .sorted(new LemmaComparator())
                .collect(Collectors.toList());
    }

    private Site getSite(String siteUrl){
        Optional<Site> siteOptional = SearchEngineRepository.siteRepository.findByUrl(siteUrl);
        if (siteOptional.isEmpty()){
            return null;
        }
        return (Site) siteOptional.get();
    }

    private Set<String> getWordsFromText(String text) throws IOException {
        return Lemmatizator.init().getLemmaSet(text);
    }

    private long getCountPages(Site site) {
        if (site == null){
            return SearchEngineRepository.pageRepository.count();
        }else {
            return SearchEngineRepository.pageRepository.countBySite(site);
        }
    }

    private List<Lemma> getLemmaList(Site site, String query) throws IOException {
        Set<String> words = getWordsFromText(query);
        if (site == null){
            List<Lemma> lemmaList = SearchEngineRepository.lemmaRepository.findAll().stream()
                    .filter(lemma -> words.contains(lemma.getLemma()))
                    .collect(Collectors.toList());
            return lemmaList;
        }else {
            List<Lemma> lemmaList = site.getLemmas().stream()
                    .filter(lemma -> words.contains(lemma.getLemma()))
                    .collect(Collectors.toList());
            return lemmaList;
        }
    }


    private static class LemmaComparator implements Comparator<Lemma>{

        @Override
        public int compare(Lemma l1, Lemma l2) {
            if (l1.getFrequency() < l2.getFrequency())
            {
                return -1;
            }
            if (l1.getFrequency()> l2.getFrequency()){
                return 1;
            }
            return 0;
        }
    }

}
