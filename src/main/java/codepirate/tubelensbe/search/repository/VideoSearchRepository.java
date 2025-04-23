package codepirate.tubelensbe.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import codepirate.tubelensbe.search.domain.VideoSearch;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


import static co.elastic.clients.elasticsearch._types.SortOrder.Desc;
import static org.apache.naming.SelectorContext.prefix;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VideoSearchRepository {

    private final ElasticsearchClient elasticsearchClient;

    public List<String> searchByPrefixSortedByViewCount(String prefix) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("tubelens_video_v4")
                    .query(q -> q
                            .prefix(p -> p
                                    .field("title.keyword")  // title.keyword 사용
                                    .value(prefix)
                            )
                    )
                    .sort(sort -> sort
                            .field(f -> f
                                    .field("viewCount")
                                    .order(SortOrder.Desc)
                            )
                    )
            );


            SearchResponse<VideoSearch> response = elasticsearchClient.search(searchRequest, VideoSearch.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .map(VideoSearch::getTitle)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Elasticsearch 검색 중 오류 발생: {}", e.getMessage());
            return List.of();
        }
    }
}
