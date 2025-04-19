package codepirate.tubelensbe.search.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoSearch {
    private String title;
    private String thumbnails;
    @Column(name = "channel_title")
    private String channelTitle;
    @Column(name = "view_count")
    private Long viewCount;

    public VideoSearch(String title, String thumbnails, String channelTitle, Long viewCount) {
        this.title = title;
        this.thumbnails = thumbnails;
        this.channelTitle = channelTitle;
        this.viewCount = viewCount;
    }
}