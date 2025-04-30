package codepirate.tubelensbe.search.dto;

import java.util.List;

public class KeywordGroup {
    public String title;
    public List<String> keywords;

    public KeywordGroup(String title, List<String> keywords) {
        this.title = title;
        this.keywords = keywords;
    }
}
