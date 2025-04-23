package codepirate.tubelensbe.suggest.controller;

import codepirate.tubelensbe.suggest.dto.SuggestionGroup;
import codepirate.tubelensbe.suggest.service.SearchSuggestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/youtube")
public class SearchSuggestController {

    private final SearchSuggestService searchService;

    public SearchSuggestController(SearchSuggestService searchSuggestService, SearchSuggestService searchService) {

        this.searchService = searchService;
    }

    @GetMapping("/suggest")
    public List<SuggestionGroup> getSuggestions(@RequestParam String keyword) throws IOException {
        return searchService.suggest(keyword);
    }
}