package codepirate.tubelensbe.video.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "tubelens_videos")
public class ESVideo {
    @Id
    @Field(type = FieldType.Keyword, name = "id")
    private String id;

    @Field(type = FieldType.Text, name = "title")
    private String title;

    @Field(type = FieldType.Text, name = "thumbnails")
    private String thumbnails;

    @Field(type = FieldType.Text, name = "embed_html")
    private String embedHtml;

    @Field(type = FieldType.Text, name = "published_at")
    private String publisedAt;

    @Field(type = FieldType.Text, name = "description")
    private String description;

    @Field(type = FieldType.Text, name = "channel_title")
    private String channelTitle;

    @Field(type = FieldType.Long, name = "view_count")
    private Long viewCount;

    @Field(type = FieldType.Long, name = "like_count")
    private Long likeCount;

    @Field(type = FieldType.Long, name = "comment_count")
    private Long commentCount;

    @Field(type = FieldType.Text, name = "updated_at")
    private String updated_at;

    @Field(type = FieldType.Dense_Vector, dims = 768)
    private float[] embedding;
}
