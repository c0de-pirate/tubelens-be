package codepirate.tubelensbe.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import codepirate.tubelensbe.video.domain.TrendingVideo;

@Repository
public interface TrendingVideoRepository extends JpaRepository<TrendingVideo, String> {

}
