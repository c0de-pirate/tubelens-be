package codepirate.tubelensbe.funnel.controller;

import codepirate.tubelensbe.funnel.dto.FunnelResponse;
import codepirate.tubelensbe.funnel.service.FunnelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/funnel")
public class FunnelController {

    private final FunnelService funnelService;

    @PostMapping()
    public FunnelResponse getFunnel(
            @RequestHeader("Authorization") String token
            ) {
        return funnelService.getFunnel(token);
    }
}
