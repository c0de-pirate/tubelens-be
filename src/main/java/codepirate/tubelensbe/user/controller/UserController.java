package codepirate.tubelensbe.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import codepirate.tubelensbe.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Object principal) {
        return switch (principal) {
            case OAuth2User oauth2User -> {
                var oauthToken = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                yield ResponseEntity.ok(userService.processOAuth2User(oauthToken, oauth2User));
            }
            case UserDetails userDetails -> ResponseEntity.ok(userService.processJwtUser(userDetails));
            case null -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            default -> {
                if (!"anonymousUser".equals(principal.toString())) {
                    yield ResponseEntity.ok(Map.of("name", principal.toString()));
                } else {
                    yield ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }
        };
    }
}