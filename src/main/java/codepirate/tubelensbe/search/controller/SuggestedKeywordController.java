package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.repository.VideoSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class SuggestedKeywordController {

    private final VideoSearchRepository videoSearchRepository;

    @GetMapping("/suggestion")
    public ResponseEntity<List<VideoSearchRepository.KeywordGroup>> getStructuredKeywordGroups(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) {

        List<VideoSearchResult> prefixResults = videoSearchRepository.searchByPrefix(keyword);
        List<VideoSearchResult> containsResults = videoSearchRepository.searchByContains(keyword);

        Set<String> seenTitles = new HashSet<>();
        List<VideoSearchRepository.KeywordGroup> keywordGroups = new ArrayList<>();

        // ✅ 1. 접두사(prefix) 검색 결과
        prefixResults.forEach(result -> {
            if (seenTitles.add(result.getTitle())) {
                List<String> tokens = extractTokens(result.getTitle(), 2, 5, 2);
                if (!tokens.isEmpty()) {
                    keywordGroups.add(new VideoSearchRepository.KeywordGroup(result.getTitle(), tokens));
                }
            }
        });

        // ✅ 2. 포함(contains) 검색 결과
        containsResults.forEach(result -> {
            if (seenTitles.add(result.getTitle())) {
                List<String> tokens = extractTokens(result.getTitle(), 2, 10, Integer.MAX_VALUE);
                if (!tokens.isEmpty()) {
                    String firstToken = tokens.get(0);
                    String matchedToken = tokens.stream()
                            .filter(t -> t.contains(keyword))
                            .findFirst()
                            .orElse(null);

                    if (matchedToken != null) {
                        List<String> selectedKeywords = firstToken.equals(matchedToken)
                                ? List.of(firstToken)
                                : List.of(firstToken, matchedToken);

                        keywordGroups.add(new VideoSearchRepository.KeywordGroup(result.getTitle(), selectedKeywords));
                    }
                }
            }
        });

        return ResponseEntity.ok(keywordGroups);
    }

    private List<String> extractTokens(String title, int minLength, int maxLength, int limit) {
        return Arrays.stream(title.split("\\s+|[^가-힣a-zA-Z0-9]"))
                .filter(token -> token.length() >= minLength && token.length() <= maxLength)
                .distinct()
                .limit(limit)
                .toList();
    }
}