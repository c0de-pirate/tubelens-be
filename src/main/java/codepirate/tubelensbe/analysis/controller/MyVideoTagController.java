package codepirate.tubelensbe.analysis.controller;

import codepirate.tubelensbe.analysis.dto.TagDto;
import codepirate.tubelensbe.analysis.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MyVideoTagController {
    private final TagService tagService;

    @PostMapping("/refresh/{channelId}")
    public List<TagDto> refreshTags(@PathVariable("channelId") String channelId, @RequestHeader("Authorization") String accessToken,
                           @RequestHeader("refreshToken") String refreshToken) throws GeneralSecurityException, IOException {
        tagService.refreshTags(channelId, accessToken, refreshToken);
        return tagService.getTags(channelId, accessToken, refreshToken);
    }

    @GetMapping("/{channelId}")
    public List<TagDto> getTags(@PathVariable("channelId") String channelId,
                                @RequestHeader("Authorization") String accessToken,
                                @RequestHeader("refreshToken") String refreshToken) throws GeneralSecurityException, IOException {
        return tagService.getTags(channelId, accessToken, refreshToken);
    }

}
