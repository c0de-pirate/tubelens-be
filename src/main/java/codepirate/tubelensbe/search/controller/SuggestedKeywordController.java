package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.dto.KeywordGroup;
import codepirate.tubelensbe.search.dto.VideoSearchResult;
import codepirate.tubelensbe.search.repository.VideoSearchRepository;
import codepirate.tubelensbe.search.service.SuggestedKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static codepirate.tubelensbe.search.util.StringTokenUtil.extractTokens;

@RestController
@RequiredArgsConstructor
@RequestMapping("/youtube")
public class SuggestedKeywordController {

    private final SuggestedKeywordService suggestedKeywordService;

    @GetMapping("suggestion")
    public ResponseEntity<List<KeywordGroup>> getSuggestedKeywords(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "low") String fuzzinessLevel) {

        List<KeywordGroup> keywordGroups = suggestedKeywordService.getStructuredKeywordGroups(keyword, fuzzinessLevel);
        return ResponseEntity.ok(keywordGroups);
    }
}