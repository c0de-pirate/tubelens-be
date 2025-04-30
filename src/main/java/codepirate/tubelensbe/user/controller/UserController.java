package codepirate.tubelensbe.user.controller;

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
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Object principal) {
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            OAuth2AuthenticationToken oauthToken =
                    (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            return ResponseEntity.ok(userService.processOAuth2User(oauthToken, oauth2User));

        } else if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return ResponseEntity.ok(userService.processJwtUser(userDetails));

        } else if (principal != null && !"anonymousUser".equals(principal.toString())) { //방어적 예외처리임 ㅇㅇ.. 지워도됨
            return ResponseEntity.ok(Map.of("name", principal.toString()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}