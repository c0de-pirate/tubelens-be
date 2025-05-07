package codepirate.tubelensbe.video.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.api.client.util.DateTime;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingVideo {
    @Id
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "thumbnails")
    private String thumbnails;

    @Column(name = "embed_html")
    private String embedHtml;

    @Column(name = "published_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private OffsetDateTime publishedAt;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "channel_title")
    private String channelTitle;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "comment_count")
    private Long commentCount;

    @ElementCollection
    private List<String> tags;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}