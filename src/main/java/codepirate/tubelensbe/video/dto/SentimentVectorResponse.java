package codepirate.tubelensbe.video.dto;

import codepirate.tubelensbe.video.domain.SentimentType;
import lombok.Data;

import java.util.ArrayList;

@Data
public class SentimentVectorResponse {
    private ArrayList<Comment> comments;

    @Data
    public static class Comment {
        private String commentId;
        private String content;
        private SentimentType sentimentType;
        private double score;
    }
}
