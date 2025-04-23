package codepirate.tubelensbe.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


import static co.elastic.clients.elasticsearch._types.SortOrder.Desc;
import static org.apache.naming.SelectorContext.prefix;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VideoSearchRepository {

    private final ElasticsearchClient elasticsearchClient;

    public List<VideoResult> searchByKeyword(String keyword) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("tubelens_video")
                    .query(q -> q
                            .bool(b -> b
                                    .should(sh -> sh.match(m -> m.field("title.ko").query(keyword)))
                                    .should(sh -> sh.match(m -> m.field("title.en").query(keyword)))
                                    .minimumShouldMatch("1")
                            )
                    )
                    .sort(sort -> sort.field(f -> f.field("view_count").order(SortOrder.Desc)))
                    .size(10)
            );

            SearchResponse<VideoSearch> response = elasticsearchClient.search(searchRequest, VideoSearch.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(v -> v != null && v.getTitle() != null)
                    .map(v -> {
                        VideoResult result = new VideoResult();
                        result.setTitle(v.getTitle());
                        result.setChannelTitle(v.getChannelTitle());
                        result.setThumbnails(v.getThumbnails());
                        result.setViewCount(v.getViewCount());
                        return result;
                    })
                    .collect(Collectors.toList());


        } catch (IOException e) {
            log.error("Elasticsearch 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }
}