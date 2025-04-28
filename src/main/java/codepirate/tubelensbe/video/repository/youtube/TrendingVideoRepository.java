package codepirate.tubelensbe.video.repository.youtube;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrendingVideoRepository extends JpaRepository<TrendingVideo, String> {
}