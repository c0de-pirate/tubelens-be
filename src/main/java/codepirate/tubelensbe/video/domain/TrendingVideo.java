package codepirate.tubelensbe.video.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.api.client.util.DateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
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

    private BigInteger viewCount ;
    
    private BigInteger likeCount ;
    
    private BigInteger commentCount ;

    @ElementCollection
    private List<String> tags;

    @UpdateTimestamp
    private LocalDateTime updated_at;
}