package codepirate.tubelensbe.video.controller;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import codepirate.tubelensbe.video.repository.TrendingVideoRepository;
import codepirate.tubelensbe.video.service.TrendingVideoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class TrendingVideoController {
    private final TrendingVideoService trendingVideoService;

    public TrendingVideoController(TrendingVideoService trendingVideoService) {
        this.trendingVideoService = trendingVideoService;
    }

    @PostMapping("/videoInsert")
    public List<TrendingVideo> videoInsert() {

    }
}
