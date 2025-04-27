package codepirate.tubelensbe.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class StructuredKeywordGroup {
    private String title;
    private List<String> keywords;
    private String text;

}