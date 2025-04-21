package codepirate.tubelensbe.search.service;


import codepirate.tubelensbe.search.domain.VideoSearch;
import codepirate.tubelensbe.search.repository.ElasticSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticSearchService {

    private final ElasticSearchRepository elasticSearchRepository;

    public List<VideoSearch> searchVideos(String keyword) throws IOException {
        return elasticSearchRepository.searchByTitle(keyword);
    }
}