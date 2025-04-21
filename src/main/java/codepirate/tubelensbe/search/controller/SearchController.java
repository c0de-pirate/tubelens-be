package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.domain.VideoSearch;
import codepirate.tubelensbe.search.service.ElasticSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
class SearchController {

    private final ElasticSearchService elasticSearchService;

    @GetMapping("/search")
    public List<VideoSearch> search(@RequestParam String keyword) throws IOException {
        return elasticSearchService.searchVideos(keyword);
    }
}