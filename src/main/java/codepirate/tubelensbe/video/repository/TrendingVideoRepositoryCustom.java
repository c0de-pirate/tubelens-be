package codepirate.tubelensbe.video.repository;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrendingVideoRepositoryCustom {
    void batchInsertIgnore(List<TrendingVideo> videoList);
}
