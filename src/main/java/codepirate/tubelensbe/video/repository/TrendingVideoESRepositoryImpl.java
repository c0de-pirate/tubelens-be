package codepirate.tubelensbe.video.repository;

import codepirate.tubelensbe.video.document.ESVideo;
import codepirate.tubelensbe.video.domain.TrendingVideo;
import codepirate.tubelensbe.video.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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

    public List<TrendingVideo> recommendVideosByTitleVectors(String videoid) {

        List<Float> queryVector = new ArrayList<>();

        log.info(videoid);

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m.match(mt -> mt.field("id").query(videoid)))
                        ))
                .build();

        List<ESVideo> response = elasticsearchOperations.search(searchQuery, ESVideo.class)
                .map(SearchHit::getContent)
                .toList();
        log.info(response.get(0).getTitle());

        return List.of();
    }
}
