package codepirate.tubelensbe.video.repository;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrendingVideoRepositoryCustom {
    void batchInsertIgnore(List<TrendingVideo> videoList);
}
