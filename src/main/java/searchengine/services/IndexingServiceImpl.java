package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.IndexPageRequest;
import searchengine.dto.share.DefaultResponse;
import searchengine.dto.share.ErrorResponse;
import searchengine.repository.SearchEngineRepository;
import searchengine.utils.SiteIndexBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    private final SitesList sitesList;

    @Override
    public DefaultResponse startIndexing() {
        if (SiteIndexBuilder.isStarted()) {
            return ErrorResponse.getErrorMessage("Индексация уже запущена");
        }
        SearchEngineRepository.siteRepository.deleteAll();
        SiteIndexBuilder.start();
        List<Site> siteListCfg = sitesList.getSites();
        List<Future<DefaultResponse>> futures = new ArrayList<>();

        for (Site site : siteListCfg) {
            SiteIndexBuilder siteIndexBuilder = new SiteIndexBuilder(site.getUrl(), site.getName());
            Future<DefaultResponse> future = (Future<DefaultResponse>) executor.submit(siteIndexBuilder);
            futures.add(future);
        }

        for (Future<DefaultResponse> future : futures) {
            try {
                DefaultResponse result = future.get();
            } catch (InterruptedException | ExecutionException e) {
            }
        }

        executor.shutdown();
        return new DefaultResponse(true);
    }

    @Override
    public DefaultResponse stopIndexing() {
        if (!SiteIndexBuilder.isStarted()) {
            return ErrorResponse.getErrorMessage("Индексация не запущена");
        }

        SiteIndexBuilder.stop();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            return ErrorResponse.getErrorMessage(e.getMessage());
        }

        return new DefaultResponse(true);
    }

    @Override
    public DefaultResponse indexPage(IndexPageRequest request) {
        String url = request.getUrl();
        List<Site> siteListCfg = sitesList.getSites();
        Site site = siteListCfg.stream()
                .filter(s -> url.replaceAll("www.", "").startsWith(s.getUrl().replaceAll("www.", "")))
                .findFirst()
                .orElse(null);

        if (site == null) {
            return ErrorResponse.getErrorMessage("Данная страница находится за пределами сайтов,\n" +
                    "указанных в конфигурационном файле");
        }

        SiteIndexBuilder siteIndexBuilder = new SiteIndexBuilder(site.getUrl(), site.getName());
        SiteIndexBuilder.start();
        DefaultResponse result = siteIndexBuilder.indexPage(url);

        return result;
    }
}
