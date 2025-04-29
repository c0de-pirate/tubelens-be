package codepirate.tubelensbe.video.controller;

import codepirate.tubelensbe.video.domain.ESVideo;
import codepirate.tubelensbe.video.domain.TrendingVideo;
import codepirate.tubelensbe.video.service.TrendingVideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/videos")
public class TrendingVideoController {
    private final TrendingVideoService trendingVideoService;

    public TrendingVideoController(TrendingVideoService trendingVideoService) {
        this.trendingVideoService = trendingVideoService;
    }

    private static final Logger log = LoggerFactory.getLogger(TrendingVideoController.class);

    @GetMapping
    @ResponseBody
    public Optional<TrendingVideo> getVideo(@RequestParam String videoId) {
        List<String> idlist = new ArrayList<>();
        idlist.add(videoId);

        return trendingVideoService.getVideo(idlist);
    }

    @GetMapping("/recomm")
    @ResponseBody
    public List<TrendingVideo> videoInsert(@RequestParam String videoId) throws IOException {
        List<String> idlist = new ArrayList<>();
        idlist.add(videoId);

        return trendingVideoService.recommVideos(idlist);
    }
}

