package codepirate.tubelensbe.video.document;

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

    @Field(type = FieldType.Keyword, name = "title")
    private String title;

    @Field(type = FieldType.Keyword, name = "thumbnails")
    private String thumbnails;

    @Field(type = FieldType.Keyword, name = "embedHtml")
    private String embedHtml;
    @Field(type = FieldType.Keyword, name = "publishedAt")
    private String publisedAt;

    @Field(type = FieldType.Keyword, name = "description")
    private String description;

    @Field(type = FieldType.Keyword, name = "channelTitle")
    private String channelTitle;

    @Field(type = FieldType.Keyword, name = "viewCount")
    private String viewCount;

    @Field(type = FieldType.Keyword, name = "likeCount")
    private String likeCount;
    @Field(type = FieldType.Keyword, name = "commentCount")
    private String commentCount;

    @Field(type = FieldType.Keyword, name = "updated_at")
    private String updated_at;

    @Field(type = FieldType.Dense_Vector, dims = 768)
    private float[] vector;

}
