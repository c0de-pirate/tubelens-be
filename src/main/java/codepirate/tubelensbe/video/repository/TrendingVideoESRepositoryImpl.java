package codepirate.tubelensbe.video.repository;

import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.json.JsonData;

import codepirate.tubelensbe.video.document.ESVideo;
import codepirate.tubelensbe.video.domain.TrendingVideo;
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

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final ElasticsearchOperations elasticsearchOperations;
//    private final String indexNamePrefix = "store-";
    private final ObjectMapper objectMapper;

    public TrendingVideoESRepositoryImpl(ElasticsearchOperations elasticsearchOperations, ObjectMapper objectMapper) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.objectMapper = objectMapper;
    }

    public List<ESVideo> recommendVideosByTitleVectors(String videoid) {
        log.info(videoid);

        NativeQuery searchById = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m.match(mt -> mt.field("id").query(videoid)))
                        ))
                .build();

        List<ESVideo> response = elasticsearchOperations.search(searchById, ESVideo.class)
                .map(SearchHit::getContent)
                .toList();

        float[] vector = response.get(0).getEmbedding();

        List<Float> embedding = new ArrayList<>();
        for (float f : vector) {
            embedding.add(f);
        }

//        Script script = Script.of(s -> s
//                .source("cosineSimilarity(params.embedding, 'embedding') + 10.0")
//                .params(Map.of("embedding", JsonData.of(embedding)))
//        );
//
//        NativeQuery searchQuery = NativeQuery.builder()
//                .withQuery(q -> q.scriptScore(ss -> ss
//                        .query(query -> query.matchAll(m -> m))
//                        .script(script)
//                ))
//                .build();
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.scriptScore(ss -> ss
                        .query(query -> query.matchAll(m -> m))
                        .script(script -> {
                                    return script
                                            .source("cosineSimilarity(params.embedding, 'embedding') + 1.0")
                                            .params(Map.of("embedding", JsonData.of(embedding)));

                                }
                        )
                ))
                .build();

        List<ESVideo> list = elasticsearchOperations.search(searchQuery, ESVideo.class)
                .map(SearchHit::getContent)
                .toList();
        log.info(String.valueOf(list.size()));

//        return elasticsearchOperations.search(searchQuery, TrendingVideo.class)
//                .map(SearchHit::getContent)
//                .toList();
        return List.of();
    }
}
