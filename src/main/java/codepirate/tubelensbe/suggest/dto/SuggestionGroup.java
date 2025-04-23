package codepirate.tubelensbe.suggest.dto;

import java.util.List;

public class SuggestionGroup {

    private List<String> keywords;
    private Long frequency;

    public SuggestionGroup(List<String> keywords, Long frequency) {
        this.keywords = keywords;
        this.frequency = frequency;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "SuggestionGroup{" +
                "keywords=" + keywords +
                ", frequency=" + frequency +
                '}';
    }
}