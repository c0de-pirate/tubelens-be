package codepirate.tubelensbe.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
public class VideoParam {
    private String part;
    private String chart;
    private String regionCode;
    private String videoCategoryId;
    private Long maxResults;
    private String key;

    public VideoParam(String part, String chart, String regionCode, String videoCategoryId, Long maxResults, String key) {
        this.part = part;
        this.chart = chart;
        this.regionCode = regionCode;
        this.videoCategoryId = videoCategoryId;
        this.maxResults = maxResults;
        this.key = key;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getVideoCategoryId() {
        return videoCategoryId;
    }

    public void setVideoCategoryId(String videoCategoryId) {
        this.videoCategoryId = videoCategoryId;
    }

    public Long getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Long maxResults) {
        this.maxResults = maxResults;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
