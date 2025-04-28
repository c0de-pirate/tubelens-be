package codepirate.tubelensbe.video.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
public class TrendingVideo {
    @Id
    private String id;

    private String title;

    private String thumbnails;

    private String embedHtml ;

    @Column(name = "published_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private OffsetDateTime publishedAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description ;

    private String channelTitle ;

    private Long viewCount ;

    private Long likeCount ;

    private Long commentCount ;

    @ElementCollection
    private List<String> tags;

    @UpdateTimestamp
    private LocalDateTime updated_at;
}
