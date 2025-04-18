package codepirate.tubelensbe.video.controller;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import codepirate.tubelensbe.video.dto.VideoParam;
import codepirate.tubelensbe.video.repository.TrendingVideoRepository;
import codepirate.tubelensbe.video.service.ApiService;
import codepirate.tubelensbe.video.service.TrendingVideoService;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.io.IOException;

@Controller
public class TrendingVideoController {
    private final TrendingVideoService trendingVideoService;
    private final ApiService apiService;

    private static final Logger log = LoggerFactory.getLogger(TrendingVideoController.class);

    public TrendingVideoController(TrendingVideoService trendingVideoService, ApiService apiService) {
        this.trendingVideoService = trendingVideoService;
        this.apiService = apiService;
    }

    @GetMapping("/videos")
    public void videoInsert(
            @RequestParam String part,
            @RequestParam String chart,
            @RequestParam String regionCode,
            @RequestParam String videoCategoryId,
            @RequestParam Long maxResults,
            @RequestParam String key
    )
            throws IOException {
        VideoParam videoParam = new VideoParam(part, chart, regionCode, videoCategoryId, maxResults, key);
        log.info(String.valueOf(maxResults));
        apiService.insertVideos(videoParam);
    }
}
