package codepirate.tubelensbe.search.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class VideoSearchResult {
    private String id;
    private String title;
    private String channelTitle;
    private String thumbnails;
    private String embedHtml;
    private long viewCount;
}
