package codepirate.tubelensbe.video.service;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import org.springframework.stereotype.Service;
import codepirate.tubelensbe.video.repository.TrendingVideoRepository;

import java.util.List;

@Service
public class TrendingVideoService {
    private final TrendingVideoRepository trendingVideoRepository;

    public TrendingVideoService(TrendingVideoRepository trendingVideoRepository) {
        this.trendingVideoRepository = trendingVideoRepository;
    }

    private void saveAll(List<TrendingVideo> trendingVideos) {
        trendingVideoRepository.saveAll(trendingVideos);
    }
}
