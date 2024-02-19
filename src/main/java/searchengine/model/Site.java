package searchengine.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "sites")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IndexStatus status;
    @Column(name = "status_time",nullable = false)
    private Date statusTime;
    @Column(name = "last_error",nullable = false ,columnDefinition="TEXT")
    private String lastError;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "site_id")
    private List<Page> pages = new ArrayList<>();

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "site_id")
    private List<Lemma> lemmas = new ArrayList<>();
}
