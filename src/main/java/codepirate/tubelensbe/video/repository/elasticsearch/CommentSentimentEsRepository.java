package codepirate.tubelensbe.video.repository.elasticsearch;

import codepirate.tubelensbe.video.domain.CommentSentiment;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentSentimentEsRepository extends ElasticsearchRepository<CommentSentiment, String> {
}
