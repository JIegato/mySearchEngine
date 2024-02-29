package searchengine.utils;

import lombok.Setter;
import org.jsoup.HttpStatusException;
import searchengine.model.Page;
import searchengine.repository.SearchEngineRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Setter
public class PageIndexBuilder extends RecursiveAction {

    private SiteGraph siteGraph;
    private String url;
    private Page page;
    private PageNode pageNode;
    private boolean indexOnlyCurrentPage;


    public static PageIndexBuilder init(SiteGraph siteGraph, String url){
        return new PageIndexBuilder(siteGraph, url);
    }
    public static PageIndexBuilder init(SiteGraph siteGraph, String url, boolean indexOnlyCurrentPage){
        return new PageIndexBuilder(siteGraph, url, indexOnlyCurrentPage);
    }

    private PageIndexBuilder(SiteGraph siteGraph, String url) {
        this.siteGraph = siteGraph;
        this.url = url;
        this.indexOnlyCurrentPage = false;
    }
    private PageIndexBuilder(SiteGraph siteGraph, String url, boolean indexOnlyCurrentPage) {
        this.siteGraph = siteGraph;
        this.url = url;
        this.indexOnlyCurrentPage = indexOnlyCurrentPage;
    }

    @Override
    protected void compute() {
        if (!SiteIndexBuilder.isStarted()) {
            return;
        };
        try {
            pageNode = PageNode.init(siteGraph, url);
        }catch (HttpStatusException e){
            page = createPage(e.getStatusCode());
            return;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        page = createPage(pageNode.getTitle(), pageNode.getContent(), pageNode.getEscapeHtmlContent());

        try {
            LemmaBuilder.buildLemmas(siteGraph.getSite(), page);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!indexOnlyCurrentPage){
            HashSet<String> childrenPage = pageNode.getChildrenPageUrl();
            ForkJoinTask.invokeAll(createSubTask(childrenPage));
        }
    }

    public void deletePageByUrl(){
        Optional pageOptional = SearchEngineRepository.pageRepository.findBySiteAndPath(siteGraph.getSite(), url);
        if (pageOptional.isEmpty()){
            return;
        }
        page = (Page) pageOptional.get();
        SearchEngineRepository.pageRepository.delete(page);
    }

    private List<PageIndexBuilder> createSubTask(HashSet<String> childrenPage){
        List<PageIndexBuilder> subTasks = new ArrayList<>();
        childrenPage.forEach(url -> {
            subTasks.add(PageIndexBuilder.init(siteGraph, url));
        });
        return subTasks;
    }

    private Page createPage(String title, String content, String htmlContent) {
        Page page = new Page();
        page.setSite(siteGraph.getSite());
        String shortUrl = getShortUrl();
        page.setPath(shortUrl);
        page.setCode(200);
        page.setContent(content);
        page.setTitle(title);
        page.setHtmlContent(htmlContent);
        SearchEngineRepository.pageRepository.saveAndFlush(page);
        return page;
    }

    private Page createPage(int statusCode){
        Page page = new Page();
        page.setSite(siteGraph.getSite());
        page.setPath(url);
        page.setCode(statusCode);
        page.setContent("");
        SearchEngineRepository.pageRepository.saveAndFlush(page);
        return page;
    }

    private String getShortUrl(){
        String siteUrl  = siteGraph.getSite().getUrl();
        return url
                .replaceAll(siteUrl,"")
                .replaceAll(siteUrl.replaceAll("www.",""),"");
    }

}
