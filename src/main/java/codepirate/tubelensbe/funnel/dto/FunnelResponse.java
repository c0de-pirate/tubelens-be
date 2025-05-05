package codepirate.tubelensbe.funnel.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FunnelResponse {
    private double EXT_URL;
    private double YT_SEARCH;
    private double RELATED_VIDEO;
    private double PLAYLIST;
    private double SUBSCRIBER;
    private double CHANNEL;
    private double NOTIFICATION;
    private double ADVERTISING;
    private double etc;
}
