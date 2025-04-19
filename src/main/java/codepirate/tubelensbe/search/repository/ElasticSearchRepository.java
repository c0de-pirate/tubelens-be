package codepirate.tubelensbe.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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

    public List<VideoSearch> searchByTitle(String keyword) throws IOException {
        // Elasticsearch에서 title을 기준으로 검색하는 쿼리 작성
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("tubelens_videos")  // 인덱스명 수정
                .query(q -> q
                        .match(m -> m
                                .field("title")
                                .query(keyword) // 사용자가 입력한 키워드로 제목 검색
                                .fuzziness("AUTO")  // 유사어 검색 가능하게
                        )
                )
        );

        // Elasticsearch에서 검색 결과를 가져오기
        SearchResponse<VideoSearch> searchResponse = elasticsearchClient.search(searchRequest, VideoSearch.class);

        // 검색 결과에서 VideoSearch 객체로 필요한 필드를 추출
        return searchResponse.hits().hits().stream()
                .map(hit -> new VideoSearch(
                        hit.source().getTitle(),        // 제목
                        hit.source().getThumbnails(),   // 썸네일
                        hit.source().getChannelTitle(), // 채널 이름
                        hit.source().getViewCount()     // 조회수
                ))
                .collect(Collectors.toList());
    }
}