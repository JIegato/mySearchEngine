package searchengine.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SearchEngineRepository {

    @Autowired
    private SiteRepository siteRep;
    @Autowired
    private PageRepository pageRep;
    @Autowired
    private LemmaRepository lemmaRep;
    @Autowired
    private IndexRepository indexRep;

    public static SiteRepository siteRepository;
    public static PageRepository pageRepository;
    public static LemmaRepository lemmaRepository;
    public static IndexRepository indexRepository;

    @PostConstruct
    public void init(){
        siteRepository = siteRep;
        pageRepository = pageRep;
        lemmaRepository = lemmaRep;
        indexRepository = indexRep;
    }

}
