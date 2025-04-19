package codepirate.tubelensbe.video.domain;


import com.fasterxml.jackson.annotation.JsonFormat;

import com.google.api.client.util.DateTime;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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


    @Column(length = 2000)
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