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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import static co.elastic.clients.elasticsearch._types.SortOrder.Desc;
import static org.apache.naming.SelectorContext.prefix;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VideoSearchRepository {

    private final ElasticsearchClient elasticsearchClient;

    public List<String> searchByKeyword(String keyword) {
        try {
            // 검색 요청 생성
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("tubelens_video")
                    .query(q -> q
                            .bool(b -> b
                                    .should(sh -> sh
                                            .match(m -> m
                                                    .field("title.ko")
                                                    .query(keyword)
                                            )
                                    )
                                    .should(sh -> sh
                                            .match(m -> m
                                                    .field("title.en")
                                                    .query(keyword)
                                            )
                                    )
                            )
                    )
                    .sort(sort -> sort
                            .field(f -> f
                                    .field("viewCount")
                                    .order(SortOrder.Desc)
                            )
                    )
            );

            // 검색 실행
            SearchResponse<VideoSearch> response = elasticsearchClient.search(searchRequest, VideoSearch.class);

            // 검색 결과에서 제목을 추출
            List<String> titles = new ArrayList<>();
            for (Hit<VideoSearch> hit : response.hits().hits()) {
                VideoSearch videoSearch = hit.source();
                if (videoSearch != null && videoSearch.getTitle() != null) {
                    titles.add(videoSearch.getTitle());
                }
            }

            return titles;
        } catch (IOException e) {
            log.error("Elasticsearch 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }
}