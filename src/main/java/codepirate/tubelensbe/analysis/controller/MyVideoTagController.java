package codepirate.tubelensbe.analysis.controller;

import codepirate.tubelensbe.analysis.dto.TagDto;
import codepirate.tubelensbe.analysis.dto.TagListDto;
import codepirate.tubelensbe.analysis.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MyVideoTagController {
    private final TagService tagService;

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshTags() throws GeneralSecurityException, IOException {
        tagService.refreshTagsIfNeeded();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wordcloud")
    public ResponseEntity<TagListDto> getWordCloud(){
        TagListDto tags = tagService.getTags();
        return ResponseEntity.ok(tags);
    }

}
