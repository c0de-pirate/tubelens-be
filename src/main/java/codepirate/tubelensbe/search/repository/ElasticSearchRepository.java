package codepirate.tubelensbe.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import codepirate.tubelensbe.search.domain.VideoSearch;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class ElasticSearchRepository {
    private final ElasticsearchClient elasticsearchClient;


    public List<String> search(String keyword) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("tubelens_videos")
                .query(q -> q
                        .bool(b -> b
                                .should(sh1 -> sh1
                                        .match(m -> m
                                                .field("title")
                                                .query(keyword)
                                                .fuzziness("AUTO")
                                        )
                                )
                                .should(sh2 -> sh2
                                        .matchPhrasePrefix(mpp -> mpp
                                                .field("title")
                                                .query(keyword)
                                        )
                                )
                        )
                )
        );

        SearchResponse<VideoSearch> searchResponse = elasticsearchClient.search(searchRequest, VideoSearch.class);

        return searchResponse.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .collect(Collectors.toList());
    }
}
