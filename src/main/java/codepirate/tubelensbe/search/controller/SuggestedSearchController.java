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
@RequestMapping
@RequiredArgsConstructor
public class SuggestedSearchController {

    private final VideoSearchService videoSearchService;

    @GetMapping("/suggested")
    public ResponseEntity<List<VideoSearchResult>> unifiedSearch(
            @RequestParam String input,
            @RequestParam List<String> keywords,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) {

        if (input == null || input.isBlank()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        // ✅ 1. 모든 키워드가 제목에 포함된 결과 (AND 검색)
        List<String> combinedKeywords = new ArrayList<>(keywords);
        combinedKeywords.add(input);

        List<VideoSearchResult> exactMatches = videoSearchService.searchByAllKeywordsInTitle(combinedKeywords);

        // ✅ 2. input OR keywords 관련 fuzzy 검색 (should 검색)
        List<VideoSearchResult> fuzzyMatches = videoSearchService.searchByInputOrKeywords(input, keywords, fuzzinessLevel);

        // ✅ 3. 중복 제거 후 exact → fuzzy 순서로 합치기
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