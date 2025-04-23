package codepirate.tubelensbe.suggest.dto;

import java.util.List;

public class SuggestionGroup {
    private List<String> keywords;

    public SuggestionGroup(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}