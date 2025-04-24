package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.dto.VideoResult;
import codepirate.tubelensbe.search.service.VideoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class VideoSearchController {

    private final VideoSearchService videoSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<VideoResult>> searchVideos(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) { // 기본값 'AUTO'
        List<VideoResult> results = videoSearchService.searchByKeyword(keyword, fuzzinessLevel);
        return ResponseEntity.ok(results);
    }
}