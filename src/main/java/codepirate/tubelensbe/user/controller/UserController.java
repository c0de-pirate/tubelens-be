package codepirate.tubelensbe.user.controller;

import codepirate.tubelensbe.auth.common.Authority;
import codepirate.tubelensbe.user.repository.UserRepository;
import codepirate.tubelensbe.user.service.UserService;
import codepirate.tubelensbe.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Object principal) {
        if (principal instanceof OAuth2User) {
            // OAuth2 로그인 사용자 처리
            OAuth2User oauth2User = (OAuth2User) principal;
            String googleId = oauth2User.getAttribute("sub");

            // 구글 ID로 사용자 정보 조회
            Optional<User> userOptional = userRepository.findByGoogleId(googleId);
            User user;

            if (userOptional.isPresent()) {
                user = userOptional.get();

                // OAuth2 토큰 가져오기
                String accessToken = null;
                String refreshToken = null;

                if (principal instanceof OAuth2AuthenticationToken) {
                    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
                    OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                            oauthToken.getAuthorizedClientRegistrationId(),
                            oauthToken.getName()
                    );

                    if (client != null) {
                        accessToken = client.getAccessToken().getTokenValue();
                        if (client.getRefreshToken() != null) {
                            refreshToken = client.getRefreshToken().getTokenValue();
                        }
                    }
                }

                // 채널 ID가 없고 토큰이 있으면 YouTube API 호출하여 채널 ID 가져오기
                if ((user.getChannel_id() == null || user.getChannel_id().isEmpty()) && accessToken != null) {
                    try {
                        String channelId = userService.fetchMyChannelId(accessToken, refreshToken);
                        if (channelId != null && !channelId.isEmpty()) {
                            user.setChannel_id(channelId);
                            userRepository.save(user);
                        }
                    } catch (Exception e) {
                        System.err.println("YouTube 채널 ID 가져오기 실패: " + e.getMessage());
                    }
                }

                // hire_date가 null이면 현재 시간으로 설정
                if (user.getHire_date() == null) {
                    user.setHire_date(new java.util.Date());
                    userRepository.save(user);
                }

                return ResponseEntity.ok(convertUserToMap(user));
            } else {
                // 새 사용자 생성
                user = new User(
                        googleId,
                        oauth2User.getAttribute("name"),
                        oauth2User.getAttribute("email"),
                        oauth2User.getAttribute("picture"),
                        Authority.ROLE_USER
                );

                // 사용자 저장
                user = userRepository.save(user);

                // OAuth2 토큰 가져오기
                String accessToken = null;
                String refreshToken = null;

                if (principal instanceof OAuth2AuthenticationToken) {
                    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
                    OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                            oauthToken.getAuthorizedClientRegistrationId(),
                            oauthToken.getName()
                    );

                    if (client != null) {
                        accessToken = client.getAccessToken().getTokenValue();
                        if (client.getRefreshToken() != null) {
                            refreshToken = client.getRefreshToken().getTokenValue();
                        }
                    }
                }

                // 액세스 토큰이 있으면 YouTube API 호출하여 채널 ID 가져오기
                if (accessToken != null) {
                    try {
                        String channelId = userService.fetchMyChannelId(accessToken, refreshToken);
                        if (channelId != null && !channelId.isEmpty()) {
                            user.setChannel_id(channelId);
                            userRepository.save(user);
                        }
                    } catch (Exception e) {
                        System.err.println("YouTube 채널 ID 가져오기 실패: " + e.getMessage());
                    }
                }

                return ResponseEntity.ok(convertUserToMap(user));
            }
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
        userInfo.put("channel_id", user.getChannel_id()); // 채널 ID 추가
        userInfo.put("hire_date", user.getHire_date()); // 가입일 추가
        return userInfo;
    }
}