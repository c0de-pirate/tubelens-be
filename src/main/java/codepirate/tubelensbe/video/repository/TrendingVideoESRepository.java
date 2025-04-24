package codepirate.tubelensbe.video.repository;

import codepirate.tubelensbe.video.document.ESVideo;
import codepirate.tubelensbe.video.domain.TrendingVideo;
import com.google.api.services.youtube.model.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendingVideoESRepository {
    List<ESVideo> recommendVideosByTitleVectors(String videoid);
}
