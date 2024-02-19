package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.*;
import searchengine.utils.StatisticViewer;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);
        total.setPages(StatisticViewer.totalCountOfPages());
        total.setLemmas(StatisticViewer.totalCountOfLemmas());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = StatisticViewer.countPagesBySiteName(site.getName());
            int lemmas = StatisticViewer.countLemmasBySiteName(site.getName());
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(StatisticViewer.getStatusBySiteName(site.getName())); //statuses[i % 3]
            item.setError(StatisticViewer.getLastErrorBySiteName(site.getName()));//errors[i % 3]
            item.setStatusTime(StatisticViewer.getStatusTimeBySiteName(site.getName())); //System.currentTimeMillis() - (random.nextInt(10_000))
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
