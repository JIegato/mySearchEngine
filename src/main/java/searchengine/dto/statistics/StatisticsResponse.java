package searchengine.dto.statistics;

import lombok.Data;
import searchengine.dto.share.DefaultResponse;

@Data
public class StatisticsResponse extends DefaultResponse {
    //private boolean result;
    private StatisticsData statistics;

    public StatisticsResponse() {
        super(true);
    }
}
