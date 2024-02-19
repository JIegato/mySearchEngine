package searchengine.services;

import searchengine.dto.IndexPageRequest;
import searchengine.dto.share.DefaultResponse;

public interface IndexingService {

    DefaultResponse startIndexing();
    DefaultResponse stopIndexing();
    DefaultResponse indexPage(IndexPageRequest request);
}
