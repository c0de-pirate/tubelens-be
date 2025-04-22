package codepirate.tubelensbe.video.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SentimentPercent {
    private double positive;
    private double neutral;
    private double negative;
}
