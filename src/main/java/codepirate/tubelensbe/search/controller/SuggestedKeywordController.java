package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.repository.VideoSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class SuggestedKeywordController {

    private final VideoSearchRepository videoSearchRepository;

    @GetMapping("/suggested/structured")
    public ResponseEntity<List<VideoSearchRepository.KeywordGroup>> getStructuredKeywordGroups(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "AUTO") String fuzzinessLevel) {

        List<VideoSearchResult> searchResults = videoSearchRepository.searchByKeyword(keyword, fuzzinessLevel);

        List<VideoSearchRepository.KeywordGroup> matched = new ArrayList<>();
        List<VideoSearchRepository.KeywordGroup> unmatched = new ArrayList<>();

        // viewCount 내림차순 정렬 후 키워드 그룹화 (키워드 최대 3개 제한)
        searchResults.stream()
                .sorted(Comparator.comparingLong(VideoSearchResult::getViewCount).reversed())
                .forEach(result -> {
                    String title = result.getTitle();
                    List<String> keywords = List.of(title.split("\\s+|[^가-힣a-zA-Z0-9]"))
                            .stream()
                            .filter(token -> token.length() >= 2)
                            .distinct()
                            .collect(Collectors.toList());
                    VideoSearchRepository.KeywordGroup group = new VideoSearchRepository.KeywordGroup(title, keywords);
                    if (keywords.contains(keyword)) {
                        matched.add(group);
                    } else {
                        unmatched.add(group);
                    }
                });

        List<VideoSearchRepository.KeywordGroup> combined = new ArrayList<>();
        combined.addAll(matched);
        combined.addAll(unmatched);

        return ResponseEntity.ok(combined);
    }
}