package codepirate.tubelensbe.search.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import codepirate.tubelensbe.search.domain.VideoSearch;
import codepirate.tubelensbe.search.repository.VideoSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static co.elastic.clients.elasticsearch._types.SortOrder.Desc;

@Service
@RequiredArgsConstructor
public class VideoSearchService {

    private final VideoSearchRepository videoSearchRepository;

    public List<String> searchByKeyword(String prefix) {
        return videoSearchRepository.searchByKeyword(prefix);
    }
}
