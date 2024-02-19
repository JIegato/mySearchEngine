package searchengine.utils;

import lombok.Getter;
import searchengine.model.Site;

import java.util.HashSet;

@Getter
public class SiteGraph {

    private Site site;
    private HashSet<String> vertex = new HashSet<>();

    public SiteGraph(Site site) {
        this.site = site;
    }

    public synchronized void addVertex(String url){
        vertex.add(url);
    }

    public synchronized boolean isExistVertex(String url){
        return vertex.contains(url);
    }
}
