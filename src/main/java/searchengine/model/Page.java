package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "pages")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(nullable = false)
    private String path;
    @Column(nullable = false)
    private int code;
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String htmlContent;
    @Column(nullable = false)
    private String title;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "site_id")
    private Site site;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "page_id")
    private List<Index> indexes = new ArrayList<>();

}
