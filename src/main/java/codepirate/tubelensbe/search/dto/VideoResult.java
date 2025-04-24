package codepirate.tubelensbe.search.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoResult {
    private String title;
    private String channelTitle;
    private String thumbnails;
    private long viewCount;
}
