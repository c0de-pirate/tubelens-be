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
            @RequestParam String input,
            @RequestParam List<String> keywords,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) {

        if (keywords == null || keywords.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        // 1. 모든 키워드가 제목에 포함된 결과
        List<VideoResult> exactMatches = videoSearchService.searchByAllKeywordsInTitle(keywords);

        // 2. 사용자가 입력한 키워드로 일반 검색
        List<VideoResult> fuzzyMatches = videoSearchService.searchByKeyword(input, fuzzinessLevel);

        // 3. 중복 제거 후 정렬: exact → fuzzy
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