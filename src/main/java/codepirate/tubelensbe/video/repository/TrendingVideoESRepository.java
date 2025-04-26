package codepirate.tubelensbe.video.repository;

import codepirate.tubelensbe.video.domain.ESVideo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendingVideoESRepository {
    List<ESVideo> recommendVideosByTitleVectors(String videoid);
}
