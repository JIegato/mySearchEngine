package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.share.DefaultResponse;
import searchengine.dto.share.ErrorResponse;
import searchengine.utils.SearchResultBuilder;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{

    @Override
    public DefaultResponse search(SearchRequest request) {
        try {
            SearchResultBuilder builder = new SearchResultBuilder();
            SearchResponse response = builder.getSearchResult(request.getSite(), request.getQuery());
            return response;
        } catch (IOException e) {
            return ErrorResponse.getErrorMessage(e.getMessage());
        }
    }
}
