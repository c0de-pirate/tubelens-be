package codepirate.tubelensbe.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TagListDto {
    private List<String> text;
    private List<Integer> size;
}
