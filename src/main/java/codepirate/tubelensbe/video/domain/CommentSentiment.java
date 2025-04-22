package codepirate.tubelensbe.video.domain;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
@Document(indexName = "comment_sentiment")
public class CommentSentiment {

    @Id
    private String id;

    @Field(name = "videoId", type = FieldType.Text)
    private String videoId;

    @Field(name = "content", type = FieldType.Text)
    private String content;

    @Field(name = "sentiment_type", type = FieldType.Text)
    private SentimentType sentimentType;

    @Field(name = "score", type = FieldType.Double)
    private Double score;
}
