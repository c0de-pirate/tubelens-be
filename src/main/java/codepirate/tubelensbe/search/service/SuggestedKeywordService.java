package codepirate.tubelensbe.search.service;

import codepirate.tubelensbe.search.dto.KeywordGroup;
import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.repository.VideoSearchRepository;
import codepirate.tubelensbe.search.util.StringTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SuggestedKeywordService {

    private final VideoSearchRepository videoSearchRepository;

    public List<KeywordGroup> getStructuredKeywordGroups(String keyword, String fuzzinessLevel) {
        List<VideoSearchResult> prefixResults = videoSearchRepository.searchByPrefix(keyword);
        List<VideoSearchResult> containsResults = videoSearchRepository.searchByContains(keyword);

        Set<String> seenKeywordSets = new HashSet<>();
        List<KeywordGroup> keywordGroups = new ArrayList<>();

        // 1. prefix 결과 처리
        prefixResults.forEach(result -> {
            List<String> tokens = StringTokenUtil.extractTokens(result.getTitle(), 2, 5, 2);
            if (!tokens.isEmpty()) {
                KeywordGroup group = new KeywordGroup(result.getTitle(), tokens);
                if (isUniqueKeywordGroup(group, seenKeywordSets)) {
                    keywordGroups.add(group);
                }
            }
        });

        // 2. contains 결과 처리
        containsResults.forEach(result -> {
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

                    KeywordGroup group = new KeywordGroup(result.getTitle(), selectedKeywords);
                    if (isUniqueKeywordGroup(group, seenKeywordSets)) {
                        keywordGroups.add(group);
                    }
                }
            }
        });

        return keywordGroups;
    }

    private boolean isUniqueKeywordGroup(KeywordGroup group, Set<String> seenKeywordSets) {
        List<String> keywords = group.getKeywords();

        if (keywords == null || keywords.isEmpty()) {
            return false;
        }

        // 정렬하여 키워드 조합의 순서가 달라도 중복으로 처리
        List<String> sortedKeywords = new ArrayList<>(keywords);
        Collections.sort(sortedKeywords);

        String key = String.join("::", sortedKeywords);
        return seenKeywordSets.add(key);
    }
}