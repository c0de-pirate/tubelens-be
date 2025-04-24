package codepirate.tubelensbe.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import codepirate.tubelensbe.search.dto.VideoResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import codepirate.tubelensbe.search.domain.VideoSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;



@Slf4j
@Repository
@RequiredArgsConstructor
public class VideoSearchRepository {

    private final ElasticsearchClient elasticsearchClient;

    public List<VideoResult> searchByKeyword(String keyword, String fuzzinessLevel) {
        try {
            // 1. 정확히 포함된 제목 (match_phrase)
            SearchResponse<VideoSearch> exactMatchResponse = elasticsearchClient.search(s -> s
                            .index("tubelens_video")
                            .query(q -> q
                                    .bool(b -> b
                                            .should(sh -> sh.matchPhrase(mp -> mp.field("title.ko").query(keyword)))
                                            .should(sh -> sh.matchPhrase(mp -> mp.field("title.en").query(keyword)))
                                            .minimumShouldMatch("1")
                                    )
                            )
                            .sort(so -> so.field(f -> f.field("view_count").order(SortOrder.Desc))),
                    VideoSearch.class
            );

            // 2. 유사어 검색 (fuzzy match) - fuzzinessLevel 파라미터로 조정
            SearchResponse<VideoSearch> fuzzyMatchResponse = elasticsearchClient.search(s -> s
                            .index("tubelens_video")
                            .query(q -> q
                                    .bool(b -> b
                                            .should(sh -> sh.match(m -> m.field("title.ko").query(keyword).fuzziness(fuzzinessLevel)))
                                            .should(sh -> sh.match(m -> m.field("title.en").query(keyword).fuzziness(fuzzinessLevel)))
                                            .minimumShouldMatch("1")
                                    )
                            )
                            .sort(so -> so.field(f -> f.field("view_count").order(SortOrder.Desc))),
                    VideoSearch.class
            );

            // 3. 결과 합치기 (정확 매칭 + 유사 매칭 중복 제거)
            Set<String> seenTitles = new HashSet<>();
            List<VideoResult> results = new ArrayList<>();

            Consumer<VideoSearch> addIfNotDuplicate = v -> {
                if (v != null && v.getTitle() != null && seenTitles.add(v.getTitle())) {
                    VideoResult result = new VideoResult();
                    result.setTitle(v.getTitle());
                    result.setChannelTitle(v.getChannelTitle());
                    result.setThumbnails(v.getThumbnails());
                    result.setViewCount(v.getViewCount());
                    results.add(result);
                }
            };

            exactMatchResponse.hits().hits().forEach(hit -> addIfNotDuplicate.accept(hit.source()));
            fuzzyMatchResponse.hits().hits().forEach(hit -> addIfNotDuplicate.accept(hit.source()));

            return results;

        } catch (IOException e) {
            log.error("Elasticsearch 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }
}