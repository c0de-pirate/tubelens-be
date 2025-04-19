package codepirate.tubelensbe.search.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoSearch {
    private String title;
    private String thumbnails;

    @JsonProperty("channel_title") // JSON에서 "channel_title"과 매핑
    private String channelTitle;

    @JsonProperty("view_count") // JSON에서 "view_count"와 매핑
    private String viewCount;
}

