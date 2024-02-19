package searchengine.utils;

import searchengine.dto.share.DefaultResponse;
import searchengine.dto.share.ErrorResponse;
import searchengine.model.IndexStatus;
import searchengine.model.Site;
import searchengine.repository.SearchEngineRepository;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SiteIndexBuilder implements Runnable{
    private String url;
    private String siteName;
    private static volatile boolean started;
    private ForkJoinPool forkJoinPool;

    public SiteIndexBuilder(String url, String siteName) {
        this.url = url;
        this.siteName = siteName;
    }

    @Override
    public void run() {
        Site site = createSite();
        SiteGraph siteGraph = new SiteGraph(site);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.execute(PageIndexBuilder.init(siteGraph, url));
        forkJoinPool.shutdown();

        ScheduledExecutorService checkService = Executors.newSingleThreadScheduledExecutor();
        checkService.scheduleAtFixedRate(()->{
            if (forkJoinPool.isTerminated()){
                site.setStatus(IndexStatus.INDEXED);
                site.setStatusTime(new Date());
                SearchEngineRepository.siteRepository.saveAndFlush(site);
                checkService.shutdownNow();
            }
        },1, 10, TimeUnit.SECONDS);

    }

    public DefaultResponse indexPage(String pageUrl){

        Optional siteOptional = SearchEngineRepository.siteRepository.findByUrl(url);
        if (siteOptional.isEmpty()){
            return ErrorResponse.getErrorMessage("Сайт ранее не индексировался.");
        }
        Site site = (Site) siteOptional.get();
        SiteGraph siteGraph = new SiteGraph(site);
        PageIndexBuilder pageIndexBuilder = PageIndexBuilder.init(siteGraph, pageUrl, true);
        pageIndexBuilder.deletePageByUrl();

        forkJoinPool = ForkJoinPool.commonPool();
        forkJoinPool.invoke(pageIndexBuilder);
        forkJoinPool.shutdown();
        return new DefaultResponse(true);
    }

    public static void start(){
        SiteIndexBuilder.started = true;
    }

    public static void stop(){
        SiteIndexBuilder.started = false;
    }

    public static boolean IsStarted(){
        return started;
    }

    private Site createSite() {
        Site site = new Site();
        site.setStatus(IndexStatus.INDEXING);
        site.setStatusTime(new Date());
        site.setLastError("");
        site.setUrl(url);
        site.setName(siteName);
        SearchEngineRepository.siteRepository.saveAndFlush(site);
        return site;
    }

}
