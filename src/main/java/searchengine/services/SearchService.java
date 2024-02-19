package searchengine.services;

import searchengine.dto.search.SearchRequest;
import searchengine.dto.share.DefaultResponse;

public interface SearchService {
    DefaultResponse search(SearchRequest request);
}
