package codepirate.tubelensbe.video.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Comment {
    private String commentId;
    private String content;
}
