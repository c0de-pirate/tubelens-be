package codepirate.tubelensbe.video.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class TrendingVideo {
    @Id
    private String id;

    private String title;

    private String thumbnails;

    private String embedHtml ;
    
    private String publishedAt ;
    
    private String description ;
    
    private String channelTitle ;
    
    private String viewCount ;
    
    private String likeCount ;
    
    private String commentCount ;

    @ElementCollection
    private List<String> tags;

    @UpdateTimestamp
    private LocalDateTime updated_at;
}
