package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.service.VideoSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class VideoSearchController {

    private final VideoSearchService videoSearchService;

    @Autowired
    public VideoSearchController(VideoSearchService videoSearchService) {
        this.videoSearchService = videoSearchService;
    }

    @GetMapping("/search")
    public List<String> searchVideos(@RequestParam String prefix) {
        return videoSearchService.searchByPrefixSortedByViewCount(prefix);
    }
}