package codepirate.tubelensbe.suggest.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
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
        // Elasticsearch 쿼리 실행
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

        // 키워드 빈도수를 추출
        Map<String, Long> keywordFrequency = new HashMap<>();

        // 응답에서 키워드 추출 및 빈도수 계산
        response.hits().hits().forEach(hit -> {
            Map<String, Object> source = (Map<String, Object>) hit.source();
            Object suggestObj = source.get("suggest");

            if (suggestObj instanceof Map suggestMap) {
                Object inputObj = suggestMap.get("input");
                if (inputObj instanceof List inputList) {
                    inputList.forEach(keyword -> {
                        keywordFrequency.put((String) keyword, keywordFrequency.getOrDefault(keyword, 0L) + 1);
                    });
                }
            }
        });

        // 빈도수 높은 순으로 정렬하여 추천 리스트 생성
        List<Map.Entry<String, Long>> sortedKeywords = keywordFrequency.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())) // 빈도수 내림차순 정렬
                .collect(Collectors.toList());

        // 내가 입력한 키워드와 빈도수가 높은 키워드 리스트 결합
        List<List<String>> suggestionGroups = new ArrayList<>();

        // 입력한 키워드를 고정하고, 빈도수가 높은 키워드를 조합
        sortedKeywords.stream()
                .filter(entry -> !entry.getKey().equals(input))  // 입력한 키워드는 제외
                .limit(10)  // 최대 10개의 키워드만 추가
                .forEach(entry -> suggestionGroups.add(Arrays.asList(input, entry.getKey())));

        return suggestionGroups;
    }
}