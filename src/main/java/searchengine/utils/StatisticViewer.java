package searchengine.utils;

import searchengine.model.Site;
import searchengine.repository.SearchEngineRepository;

import java.util.Optional;

public class StatisticViewer {
    public static int totalCountOfPages(){
        return (int) SearchEngineRepository.pageRepository.count();
    }

    public static int totalCountOfLemmas() {
        return (int) SearchEngineRepository.lemmaRepository.count();
    }

    public static int countPagesBySiteName(String name) {
        return (int) SearchEngineRepository.pageRepository.countBySite_Name(name);
    }

    public static int countLemmasBySiteName(String name) {
        return (int) SearchEngineRepository.lemmaRepository.countBySite_Name(name);
    }

    public static String getStatusBySiteName(String name) {
        Optional siteOptional = SearchEngineRepository.siteRepository.findByName(name);
        if (siteOptional.isEmpty()) return "FAILED";
        Site site = (Site) siteOptional.get();
        return site.getStatus().toString();
    }

    public static String getLastErrorBySiteName(String name) {
        Optional siteOptional = SearchEngineRepository.siteRepository.findByName(name);
        if (siteOptional.isEmpty()) return "";
        Site site = (Site) siteOptional.get();
        return site.getLastError();
    }

    public static long getStatusTimeBySiteName(String name) {
        Optional siteOptional = SearchEngineRepository.siteRepository.findByName(name);
        if (siteOptional.isEmpty()) return 0L;
        Site site = (Site) siteOptional.get();
        return site.getStatusTime().getTime();
    }
}
