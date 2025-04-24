package codepirate.tubelensbe.suggest.controller;

import codepirate.tubelensbe.suggest.service.KeywordSuggestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/youtube")
public class KeywordSuggestController {

    private final KeywordSuggestService searchService;

    public KeywordSuggestController(KeywordSuggestService keywordSuggestService, KeywordSuggestService searchService) {

        this.searchService = searchService;
    }

    @GetMapping("/suggestions")
    public List<List<String>> getSuggestions(@RequestParam String keyword) throws IOException {
        return searchService.suggest(keyword);
    }
}