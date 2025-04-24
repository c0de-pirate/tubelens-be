package codepirate.tubelensbe.search.service;


import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.repository.VideoSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoSearchService {

    private final VideoSearchRepository videoSearchRepository;

    public List<VideoSearchResult> searchByKeyword(String keyword, String fuzzinessLevel) {
        return videoSearchRepository.searchByKeyword(keyword, fuzzinessLevel);
    }

    public List<VideoSearchResult> searchByAllKeywordsInTitle(List<String> keywords) {
        return videoSearchRepository.searchByAllKeywordsInTitle(keywords);
    }
}