package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Optional<Page> findBySiteAndPath(Site site, String path);

    long countBySite_Name(String name);

    @Query("select count(p) from Page p where p.site = ?1")
    long countBySite(Site site);

}
