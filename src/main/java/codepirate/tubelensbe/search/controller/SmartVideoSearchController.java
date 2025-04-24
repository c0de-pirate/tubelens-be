package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.dto.VideoResult;
import codepirate.tubelensbe.search.service.VideoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class SmartVideoSearchController {

    private final VideoSearchService videoSearchService;

    @GetMapping("/smart/search")
    public ResponseEntity<List<VideoResult>> unifiedSearch(
            @RequestParam List<String> keywords,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) {

        if (keywords == null || keywords.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        // 1. 일반 검색 수행 (첫 번째 키워드 기반)
        String primaryKeyword = keywords.get(0);
        List<VideoResult> fuzzyMatches = videoSearchService.searchByKeyword(primaryKeyword, fuzzinessLevel);

        // 2. fuzzyMatches 중 정확히 모든 키워드를 포함한 결과만 필터링
        List<VideoResult> exactMatches = fuzzyMatches.stream()
                .filter(video -> keywords.stream().allMatch(k -> video.getTitle().contains(k)))
                .collect(Collectors.toList());

        // 3. 중복 제거 및 정렬
        Set<String> seenTitles = new HashSet<>();
        List<VideoResult> combinedResults = new ArrayList<>();

        for (VideoResult result : exactMatches) {
            if (seenTitles.add(result.getTitle())) {
                combinedResults.add(result);
            }
        }

        for (VideoResult result : fuzzyMatches) {
            if (seenTitles.add(result.getTitle())) {
                combinedResults.add(result);
            }
        }

        return ResponseEntity.ok(combinedResults);
    }
}