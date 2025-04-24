package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.service.VideoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class SuggestedSearchController {

    private final VideoSearchService videoSearchService;

    @GetMapping("/search/suggested")
    public ResponseEntity<List<VideoSearchResult>> unifiedSearch(
            @RequestParam String input,
            @RequestParam List<String> keywords,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) {

        if (keywords == null || keywords.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        // 1. 모든 키워드가 제목에 포함된 결과
        List<VideoSearchResult> exactMatches = videoSearchService.searchByAllKeywordsInTitle(keywords);

        // 2. 사용자가 입력한 키워드로 일반 검색
        List<VideoSearchResult> fuzzyMatches = videoSearchService.searchByKeyword(input, fuzzinessLevel);

        // 3. 중복 제거 후 정렬: exact → fuzzy
        Set<String> seenTitles = new HashSet<>();
        List<VideoSearchResult> combinedResults = new ArrayList<>();

        for (VideoSearchResult result : exactMatches) {
            if (seenTitles.add(result.getTitle())) {
                combinedResults.add(result);
            }
        }

        for (VideoSearchResult result : fuzzyMatches) {
            if (seenTitles.add(result.getTitle())) {
                combinedResults.add(result);
            }
        }

        return ResponseEntity.ok(combinedResults);
    }
}