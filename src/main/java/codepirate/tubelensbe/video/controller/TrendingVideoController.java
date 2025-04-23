package codepirate.tubelensbe.video.controller;

import codepirate.tubelensbe.video.service.TrendingVideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;


@Controller
@RequestMapping("/videos")
public class TrendingVideoController {
    private final TrendingVideoService trendingVideoService;

    public TrendingVideoController(TrendingVideoService trendingVideoService) {
        this.trendingVideoService = trendingVideoService;
    }

    private static final Logger log = LoggerFactory.getLogger(TrendingVideoController.class);

    @GetMapping("/recomm")
//    @ResponseBody
    public void videoInsert(@RequestParam String videoid) throws IOException {
        List<String> idlist = new ArrayList<>();
        idlist.add(videoid);
        trendingVideoService.recommVideos(idlist);
    }
}

