package codepirate.tubelensbe.search.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoSearch {
    private String title;
    private String thumbnails;
    @JsonProperty("channel_title")
    private String channelTitle;
    @JsonProperty("view_count")
    private Long viewCount;
}