package codepirate.tubelensbe.video.controller;

import codepirate.tubelensbe.video.dto.SentimentPercent;
import codepirate.tubelensbe.video.service.SentimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sentiment")
@RequiredArgsConstructor
public class SentimentController {

    private final SentimentService sentimentService;

    @GetMapping("/{videoId}")
    public ResponseEntity<SentimentPercent> sentimentComments(@PathVariable String videoId) {
        return ResponseEntity.ok(sentimentService.getYoutubeComments(videoId));
    }
}
