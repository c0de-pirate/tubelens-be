package codepirate.tubelensbe.suggest.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import codepirate.tubelensbe.suggest.dto.SuggestionGroup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchSuggestService {

    private final ElasticsearchClient elasticsearchClient;

    public SearchSuggestService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public List<SuggestionGroup> suggest(String input) throws IOException {
        SearchResponse<Object> response = elasticsearchClient.search(s -> s
                        .index("tubelens_video")
                        .query(q -> q
                                .match(m -> m
                                        .field("suggest")
                                        .query(input)
                                )
                        )
                        .size(10),
                Object.class
        );

        return response.hits().hits().stream()
                .map(hit -> {
                    Map<String, Object> source = (Map<String, Object>) hit.source();
                    Object suggestObj = source.get("suggest");

                    if (suggestObj instanceof Map suggestMap) {
                        Object inputObj = suggestMap.get("input");
                        if (inputObj instanceof List inputList) {
                            return new SuggestionGroup((List<String>) inputList);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}