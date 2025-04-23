package codepirate.tubelensbe.search.controller;

import codepirate.tubelensbe.search.service.VideoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class VideoSearchController {

    private final VideoSearchService videoSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<String>> searchVideos(@RequestParam String prefix) {
        List<String> results = videoSearchService.searchByKeyword(prefix);
        return ResponseEntity.ok(results);
    }
}