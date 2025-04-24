package codepirate.tubelensbe.search.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoSearchResult {
    private String title;
    private String channelTitle;
    private String thumbnails;
    private long viewCount;
}
