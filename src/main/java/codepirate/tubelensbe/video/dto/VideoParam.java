package codepirate.tubelensbe.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoParam {
    private String part;
    private String chart;
    private String regionCode;
    private String videoCategoryId;
    private Long maxResults;
    private String key;
}

