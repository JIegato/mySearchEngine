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
    ExecutorService executors;
    private static final int THREADS = Runtime.getRuntime().availableProcessors() / 2;
    private final SitesList sitesList;

    @Override
    public DefaultResponse startIndexing() {
        if (SiteIndexBuilder.isStarted()) {
            stopIndexing();
        }
        SearchEngineRepository.siteRepository.deleteAll();
        SiteIndexBuilder.start();
        List<Site> siteListCfg = sitesList.getSites();
        List<Future<DefaultResponse>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        executors = executor;
        for (Site site : siteListCfg) {
            SiteIndexBuilder siteIndexBuilder = new SiteIndexBuilder(site.getUrl(), site.getName());
            Future<DefaultResponse> future = (Future<DefaultResponse>) executors.submit(siteIndexBuilder);
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
        SiteIndexBuilder.stop();
        executors.shutdown();
        try {
            if (!executors.awaitTermination(10, TimeUnit.SECONDS)) {
                executors.shutdown();
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
