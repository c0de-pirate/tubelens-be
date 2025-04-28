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

        // 1. fuzzy 검색
        List<VideoSearchResult> allResults = videoSearchService.searchByInputOrKeywords(input, keywords, fuzzinessLevel);

        // 2. 완전 일치 필터
        Set<String> perfectMatchTitles = allResults.stream()
                .filter(result -> {
                    String title = result.getTitle();
                    return title != null && title.contains(input) && keywords.stream().allMatch(title::contains);
                })
                .map(VideoSearchResult::getTitle)
                .collect(Collectors.toSet());

        // 3. 중복 없이 perfectMatches + 나머지 fuzzy 결과 합치기
        Set<String> seenTitles = new HashSet<>();
        List<VideoSearchResult> combinedResults = new ArrayList<>();

        allResults.stream()
                .sorted((r1, r2) -> {
                    boolean r1Perfect = perfectMatchTitles.contains(r1.getTitle());
                    boolean r2Perfect = perfectMatchTitles.contains(r2.getTitle());
                    return Boolean.compare(r2Perfect, r1Perfect); // perfectMatch 우선 정렬
                })
                .filter(result -> result.getTitle() != null && seenTitles.add(result.getTitle()))
                .forEach(combinedResults::add);

        return ResponseEntity.ok(combinedResults);
    }
}