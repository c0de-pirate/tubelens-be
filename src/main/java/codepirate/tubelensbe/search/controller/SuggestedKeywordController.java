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
        for (VideoSearchResult result : prefixResults) {
            if (seenTitles.add(result.getTitle())) {
                String title = result.getTitle();
                List<String> tokens = Arrays.stream(title.split("\\s+|[^가-힣a-zA-Z0-9]"))
                        .filter(token -> token.length() >= 2 && token.length() <= 5)
                        .distinct()
                        .limit(2)
                        .toList();

                if (!tokens.isEmpty()) {
                    keywordGroups.add(new VideoSearchRepository.KeywordGroup(title, tokens));
                }
            }
        }

        // ✅ 2. 포함(contains) 검색 결과 (수정 포인트)
        for (VideoSearchResult result : containsResults) {
            if (seenTitles.add(result.getTitle())) {
                String title = result.getTitle();

                List<String> tokens = Arrays.stream(title.split("\\s+|[^가-힣a-zA-Z0-9]"))
                        .filter(token -> token.length() >= 2 && token.length() <= 10)
                        .toList();

                if (tokens.isEmpty()) continue;

                String firstToken = tokens.get(0);
                String matchedToken = null;

                for (String token : tokens) {
                    if (token.contains(keyword)) {
                        matchedToken = token;
                        break;
                    }
                }

                if (matchedToken != null) {
                    List<String> selectedKeywords = new ArrayList<>();

                    if (!firstToken.equals(matchedToken)) {
                        selectedKeywords.add(firstToken);
                        selectedKeywords.add(matchedToken);
                    } else {
                        selectedKeywords.add(firstToken); // 같으면 하나만
                    }

                    keywordGroups.add(new VideoSearchRepository.KeywordGroup(title, selectedKeywords));
                }
            }
        }

        return ResponseEntity.ok(keywordGroups);
    }
}