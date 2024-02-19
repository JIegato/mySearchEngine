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

import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{

    private static final int THREADS = 4;
    private static ExecutorService executor;
    private final SitesList sitesList;

    @Override
    public DefaultResponse startIndexing() {
        if (SiteIndexBuilder.IsStarted()){
            return ErrorResponse.getErrorMessage("Индексация уже запущена");
        }
        SearchEngineRepository.siteRepository.deleteAll();
        executor = Executors.newFixedThreadPool(THREADS);
        SiteIndexBuilder.start();
        List<Site> siteListCfg = sitesList.getSites();
        siteListCfg.forEach(site -> {
            executor.execute(new SiteIndexBuilder(site.getUrl(), site.getName()));
        });
        return new DefaultResponse(true);
    }

    @Override
    public DefaultResponse stopIndexing() {
        if (!SiteIndexBuilder.IsStarted()){
            return ErrorResponse.getErrorMessage("Индексация не запущена");
        }
        SiteIndexBuilder.stop();
        executor.shutdown();
        try {
            executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e){
            return ErrorResponse.getErrorMessage(e.getMessage());
        }finally {
            if (!executor.isTerminated()){
                executor.shutdownNow();
            }
        }
        return new DefaultResponse(true);
    }

    @Override
    public DefaultResponse indexPage(IndexPageRequest request) {
        String url = request.getUrl();
        List<Site> siteListCfg = sitesList.getSites();
        Site site = siteListCfg
                .stream()
                .filter(s -> url.replaceAll("www.","").startsWith(s.getUrl().replaceAll("www.","")))
                .findFirst()
                .orElse(null);

        if (site == null){
            return ErrorResponse.getErrorMessage("Данная страница находится за пределами сайтов,\n" +
                    "указанных в конфигурационном файле");
        }
        SiteIndexBuilder.start();
        SiteIndexBuilder siteIndexBuilder = new SiteIndexBuilder(site.getUrl(), site.getName());
        DefaultResponse result = siteIndexBuilder.indexPage(url);
        return result;
    }
}
