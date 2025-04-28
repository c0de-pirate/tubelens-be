package codepirate.tubelensbe.search.dto;

import java.util.List;

public class VideoSearchResponse {
    private List<VideoSearchResult> results;
    private String suggestion;

    public VideoSearchResponse(List<VideoSearchResult> results, String suggestion) {
        this.results = results;
        this.suggestion = suggestion;
    }

    public List<VideoSearchResult> getResults() {
        return results;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
