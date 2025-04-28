package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.service.VideoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class SuggestedSearchController {

    private final VideoSearchService videoSearchService;

    @GetMapping("/suggested/structured")
    public ResponseEntity<List<VideoSearchResult>> unifiedSearch(
            @RequestParam String input,
            @RequestParam List<String> keywords,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) {

        if (input == null || input.isBlank()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        // fuzzy로 먼저 전체 검색
        List<VideoSearchResult> allResults = videoSearchService.searchByInputOrKeywords(input, keywords, fuzzinessLevel);

        // 🔥 완전 일치만 필터링
        List<VideoSearchResult> perfectMatches = allResults.stream()
                .filter(result -> {
                    String title = result.getTitle();
                    return title.contains(input) && keywords.stream().allMatch(title::contains);
                })
                .collect(Collectors.toList());

        // 중복 제거
        Set<String> seenTitles = new HashSet<>();
        List<VideoSearchResult> combinedResults = new ArrayList<>();

        // perfectMatches 먼저 추가
        for (VideoSearchResult result : perfectMatches) {
            if (seenTitles.add(result.getTitle())) {
                combinedResults.add(result);
            }
        }

        // 나머지 fuzzy 결과 추가
        for (VideoSearchResult result : allResults) {
            if (seenTitles.add(result.getTitle())) {
                combinedResults.add(result);
            }
        }

        return ResponseEntity.ok(combinedResults);
    }
}