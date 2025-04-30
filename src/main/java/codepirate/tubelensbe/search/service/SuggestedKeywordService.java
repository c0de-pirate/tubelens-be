package codepirate.tubelensbe.search.service;

import codepirate.tubelensbe.search.dto.KeywordGroup;
import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.repository.VideoSearchRepository;
import codepirate.tubelensbe.search.util.StringTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SuggestedKeywordService {

    private final VideoSearchRepository videoSearchRepository;

    public List<KeywordGroup> getStructuredKeywordGroups(String keyword, String fuzzinessLevel) {
        List<VideoSearchResult> prefixResults = videoSearchRepository.searchByPrefix(keyword);
        List<VideoSearchResult> containsResults = videoSearchRepository.searchByContains(keyword);

        Set<String> seenTitles = new HashSet<>();
        List<KeywordGroup> keywordGroups = new ArrayList<>();

        // 1. prefix 결과 처리
        prefixResults.forEach(result -> {
            if (seenTitles.add(result.getTitle())) {
                List<String> tokens = StringTokenUtil.extractTokens(result.getTitle(), 2, 5, 2);
                if (!tokens.isEmpty()) {
                    keywordGroups.add(new KeywordGroup(result.getTitle(), tokens));
                }
            }
        });

        // 2. contains 결과 처리
        containsResults.forEach(result -> {
            if (seenTitles.add(result.getTitle())) {
                List<String> tokens = StringTokenUtil.extractTokens(result.getTitle(), 2, 10, Integer.MAX_VALUE);
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

                        keywordGroups.add(new KeywordGroup(result.getTitle(), selectedKeywords));
                    }
                }
            }
        });

        return keywordGroups;
    }
}
