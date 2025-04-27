package codepirate.tubelensbe.user.controller;

import codepirate.tubelensbe.user.repository.UserRepository;
import codepirate.tubelensbe.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository; // User 엔티티 리포지토리 주입

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Object principal) {
        if (principal instanceof OAuth2User) {
            // OAuth2 로그인 사용자 처리
            OAuth2User oauth2User = (OAuth2User) principal;
            String googleId = oauth2User.getAttribute("sub");

            // 구글 ID로 사용자 정보 조회
            Optional<User> userOptional = userRepository.findByGoogleId(googleId);

            if (userOptional.isPresent()) {
                return ResponseEntity.ok(convertUserToMap(userOptional.get()));
            }

            // DB에 없으면 OAuth2 속성 반환
            return ResponseEntity.ok(oauth2User.getAttributes());
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            // JWT 인증 사용자 처리
            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) principal;
            String username = userDetails.getUsername();

            // 사용자 이름으로 DB에서 사용자 정보 조회
            Optional<User> userOptional = userRepository.findByName(username);

            if (userOptional.isPresent()) {
                return ResponseEntity.ok(convertUserToMap(userOptional.get()));
            }

            // 기본 정보 반환
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("name", username);
            return ResponseEntity.ok(basicInfo);
        } else if (principal != null && !"anonymousUser".equals(principal.toString())) {
            // 다른 타입의 인증 처리
            return ResponseEntity.ok(Map.of("name", principal.toString()));
        }

        // 인증되지 않은 사용자 처리
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 사용자 정보를 Map으로 변환하는 유틸리티 메서드
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());

        String pictureUrl = user.getPicture();
        if (pictureUrl != null && !pictureUrl.startsWith("http")) {
            pictureUrl = "https://lh3.googleusercontent.com/a/" + pictureUrl;
        }
        userInfo.put("picture", pictureUrl);
        userInfo.put("googleId", user.getGoogleId());
        return userInfo;
    }
}