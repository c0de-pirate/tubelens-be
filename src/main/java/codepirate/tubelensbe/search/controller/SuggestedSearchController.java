package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.service.VideoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class SuggestedSearchController {

    private final VideoSearchService videoSearchService;

    @GetMapping("/suggested/structured")
    public ResponseEntity<List<VideoSearchResult>> getSuggestedVideos(
            @RequestParam String input,
            @RequestParam List<String> keywords,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) {

        if (input == null || input.isBlank()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        List<VideoSearchResult> results = videoSearchService.searchWithPriority(input, keywords, fuzzinessLevel);
        return ResponseEntity.ok(results);
    }
}