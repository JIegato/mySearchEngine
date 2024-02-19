package searchengine.dto.search;

import lombok.Getter;
import lombok.Setter;
import searchengine.dto.share.DefaultResponse;

import java.util.List;

@Getter
@Setter
public class SearchResponse extends DefaultResponse {
    private int count;
    private List<SearchResulItem> data;
    public SearchResponse() {
        super(true);
    }


}
