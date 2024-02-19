package searchengine.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Site {
    private String url;
    private String name;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Site site = (Site) obj;
        return site
                .getUrl()
                .replaceAll("www.","")
                .startsWith(this
                        .getUrl()
                        .replaceAll("www.",""));
    }
}
