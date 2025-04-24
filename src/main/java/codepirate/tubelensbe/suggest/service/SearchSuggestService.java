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

    public List<List<String>> suggest(String input) throws IOException {
        SearchResponse<Map> response = elasticsearchClient.search(s -> s
                        .index("tubelens_video")
                        .query(q -> q
                                .match(m -> m
                                        .field("suggest")
                                        .query(input)
                                )
                        )
                        .size(50),
                Map.class
        );

        Set<String> seenPairs = new HashSet<>();
        List<List<String>> suggestionGroups = new ArrayList<>();

        for (var hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();
            if (source == null) continue;

            String title = (String) source.get("title");
            if (title == null) continue;

            Object suggestObj = source.get("suggest");
            if (!(suggestObj instanceof Map suggestMap)) continue;

            Object inputObj = suggestMap.get("input");
            if (!(inputObj instanceof List inputList)) continue;

            // 입력 키워드 제외한 나머지 후보 필터링
            List<String> candidates = (List<String>) inputList.stream()
                    .filter(o -> o instanceof String)
                    .map(String.class::cast)
                    .filter(k -> !k.equals(input))
                    .distinct()
                    .collect(Collectors.toList());

            // input + candidates => 실제 title 내 위치 순으로 정렬
            List<String> allKeywords = new ArrayList<>();
            allKeywords.add(input);
            allKeywords.addAll(candidates);

            allKeywords.sort(Comparator.comparingInt(k -> {
                int idx = title.indexOf(k);
                return idx < 0 ? Integer.MAX_VALUE : idx;
            }));

            // 최소 2개 이상이어야 조합 가능
            if (allKeywords.size() >= 2) {
                List<String> pair = allKeywords.subList(0, 2);
                String key = String.join("::", pair);
                if (seenPairs.add(key)) {
                    suggestionGroups.add(new ArrayList<>(pair));
                }
            }

            if (suggestionGroups.size() >= 10) break;
        }

        return suggestionGroups;
    }
}