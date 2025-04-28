package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.service.VideoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class BasicSearchController {

    private final VideoSearchService videoSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<VideoSearchResult>> searchVideos(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) { // 기본값 'AUTO'
        List<VideoSearchResult> results = videoSearchService.searchByKeyword(keyword, fuzzinessLevel);
        return ResponseEntity.ok(results);
    }
}