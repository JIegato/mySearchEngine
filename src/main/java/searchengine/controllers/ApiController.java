package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.IndexPageRequest;
import searchengine.dto.search.SearchRequest;
import searchengine.dto.share.DefaultResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService,
                         IndexingService indexingService,
                         SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<DefaultResponse> startIndexing(){
        return ResponseEntity.ok(indexingService.startIndexing());
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<DefaultResponse> stopIndexing(){
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<DefaultResponse> indexPage(@ModelAttribute IndexPageRequest request){
        return ResponseEntity.ok(indexingService.indexPage(request));
    }
    @GetMapping("/search")
    public ResponseEntity<DefaultResponse> search(@ModelAttribute SearchRequest searchRequest){
        return ResponseEntity.ok(searchService.search(searchRequest));
    }
}
