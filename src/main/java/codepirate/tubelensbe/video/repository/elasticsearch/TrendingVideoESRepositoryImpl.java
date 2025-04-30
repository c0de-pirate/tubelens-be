package codepirate.tubelensbe.video.repository.elasticsearch;

import co.elastic.clients.json.JsonData;

import codepirate.tubelensbe.video.domain.ESVideo;
import codepirate.tubelensbe.video.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class TrendingVideoESRepositoryImpl implements TrendingVideoESRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public TrendingVideoESRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<ESVideo> recommendVideosByTitleVectors(String videoid) {
        String tempID = "_C_xG10JwXs";

        // 1. 입력 받은 videoid로 검색
        NativeQuery searchById = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m.match(mt -> mt.field("id").query(videoid)))
                        ))
                .build();

        List<ESVideo> response = elasticsearchOperations.search(searchById, ESVideo.class)
                .map(SearchHit::getContent)
                .toList();

        float[] vector;

        // 검색 결과가 없거나 embedding이 비어있는 경우 tempID로 대체
        if (response.isEmpty() || response.get(0).getEmbedding() == null || response.get(0).getEmbedding().length == 0) {
            NativeQuery searchByTempId = NativeQuery.builder()
                    .withQuery(q -> q
                            .bool(b -> b
                                    .must(m -> m.match(mt -> mt.field("id").query(tempID)))
                            ))
                    .build();

            List<ESVideo> tempResponse = elasticsearchOperations.search(searchByTempId, ESVideo.class)
                    .map(SearchHit::getContent)
                    .toList();

            if (tempResponse.isEmpty() || tempResponse.get(0).getEmbedding() == null) {
                return List.of(); // 둘 다 임베딩이 없으면 빈 리스트 반환
            }

            vector = tempResponse.get(0).getEmbedding();
        } else {
            vector = response.get(0).getEmbedding();
        }

        List<Float> embedding = new ArrayList<>();
        for (float f : vector) {
            embedding.add(f);
        }

        // 2. 검색 결과 수를 5개로 제한
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(must -> must
                                .scriptScore(ss -> ss
                                        .query(query -> query.matchAll(m -> m))
                                        .script(script -> script
                                                .source("cosineSimilarity(params.embedding, 'embedding') + 1.0")
                                                .params(Map.of("embedding", JsonData.of(embedding)))
                                        )
                                )
                        )
                        .filter(filter -> filter
                                .exists(exists -> exists
                                        .field("embedding")
                                )
                        )
                        .mustNot(mustnot -> mustnot
                                .ids(id -> id
                                        .values(videoid)
                                )
                        )
                ))
                .withPageable(PageRequest.of(0, 5)) // 결과를 5개로 제한
                .build();

        List<ESVideo> list = elasticsearchOperations.search(searchQuery, ESVideo.class)
                .map(SearchHit::getContent)
                .toList();

        return list; // 빈 리스트 대신 실제 검색 결과 반환
    }
}