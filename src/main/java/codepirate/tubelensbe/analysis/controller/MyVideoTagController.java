package codepirate.tubelensbe.analysis.controller;

import codepirate.tubelensbe.analysis.dto.TagDto;
import codepirate.tubelensbe.analysis.service.TagService;
import codepirate.tubelensbe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
@RequestMapping("/mypage")
@RestController
@RequiredArgsConstructor
public class MyVideoTagController {
    private final TagService tagService;
    private final UserService userService;

    @PostMapping("/refresh")
    public List<TagDto> refreshTags(@RequestHeader("Authorization") String token,
                           @RequestHeader("refreshToken") String refreshToken) throws GeneralSecurityException, IOException {
        String channelId = userService.findByChannelId(refreshToken);
        return tagService.refreshTags(channelId, token, refreshToken);
    }

    @GetMapping
    public List<TagDto> getTags(@RequestHeader("Authorization") String token,
                                @RequestHeader("refreshToken") String refreshToken) throws GeneralSecurityException, IOException {
        String channelId = userService.findByChannelId(refreshToken);
        return tagService.getTags(channelId, token, refreshToken);
    }

}
