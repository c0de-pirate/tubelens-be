package codepirate.tubelensbe.search.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class KeywordGroup {
    public String title;
    public List<String> keywords;

    public KeywordGroup(String title, List<String> keywords) {
        this.title = title;
        this.keywords = keywords;
    }
}
