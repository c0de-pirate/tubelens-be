package codepirate.tubelensbe.search.service;


import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.repository.VideoSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoSearchService {

    private final VideoSearchRepository videoSearchRepository;

    public List<VideoSearchResult> searchByKeyword(String keyword, String fuzzinessLevel) {
        return videoSearchRepository.searchByKeyword(keyword, fuzzinessLevel);
    }

    public List<VideoSearchResult> searchByInputOrKeywords(String input, List<String> keywords, String fuzzinessLevel) {
        return videoSearchRepository.searchByInputOrKeywords(input, keywords, fuzzinessLevel);
    }

    public List<VideoSearchResult> searchWithPriority(String input, List<String> keywords, String fuzzinessLevel) {
        List<VideoSearchResult> allResults = searchByInputOrKeywords(input, keywords, fuzzinessLevel);

        Set<String> perfectMatchTitles = allResults.stream()
                .filter(result -> isPerfectMatch(result, input, keywords))
                .map(VideoSearchResult::getTitle)
                .collect(Collectors.toSet());

        Set<String> seenTitles = new HashSet<>();
        return allResults.stream()
                .sorted(byPerfectMatchFirst(perfectMatchTitles))
                .filter(result -> result.getTitle() != null && seenTitles.add(result.getTitle()))
                .toList();
    }

    private boolean isPerfectMatch(VideoSearchResult result, String input, List<String> keywords) {
        String title = result.getTitle();
        return title != null && title.contains(input) && keywords.stream().allMatch(title::contains);
    }

    private Comparator<VideoSearchResult> byPerfectMatchFirst(Set<String> perfectTitles) {
        return Comparator.comparing((VideoSearchResult r) -> perfectTitles.contains(r.getTitle()))
                .reversed(); // true 우선
    }
}