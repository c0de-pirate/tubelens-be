package codepirate.tubelensbe.auth.oauth2.handler;

import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.auth.oauth2.service.CustomOAuth2UserService;
import codepirate.tubelensbe.auth.jwt.JwtTokenProvider;
import codepirate.tubelensbe.auth.refreshtoken.RefreshToken;
import codepirate.tubelensbe.auth.refreshtoken.RefreshTokenService;
import codepirate.tubelensbe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpirySeconds;

    @Autowired
    private codepirate.tubelensbe.user.repository.UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("{server.react.port}")
    private String port;

    @Autowired
    private HttpSessionOAuth2AuthorizationRequestRepository httpSessionOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = processOAuth2User(oAuth2User, authentication);

        String accessToken = jwtTokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(user.getName(), null, oAuth2User.getAuthorities()));
        RefreshToken refreshToken;

        Optional<RefreshToken> existingTokenOpt = refreshTokenService.findByUserId(user.getId());

        if (existingTokenOpt.isPresent()) {
            RefreshToken existingToken = existingTokenOpt.get();
            if (!refreshTokenService.isTokenExpired(existingToken)) {
                refreshToken = existingToken;
            } else {
                refreshToken = refreshTokenService.createRefreshToken(user);
            }
        } else {
            refreshToken = refreshTokenService.createRefreshToken(user);
        }

        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:" + port +"/")
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken.getToken())
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }

    private User processOAuth2User(OAuth2User oAuth2User, Authentication authentication) {
        // oAuth2User의 속성을 이용하여 User 엔티티를 생성하거나 조회하는 로직
        codepirate.tubelensbe.auth.oauth2.user.GoogleOAuth2UserInfo oAuth2UserInfo = new codepirate.tubelensbe.auth.oauth2.user.GoogleOAuth2UserInfo(oAuth2User.getAttributes());

        java.util.Optional<User> userOptional = userRepository.findByGoogleId(oAuth2UserInfo.getId());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getEmail().equals(oAuth2UserInfo.getEmail())) {
                throw new RuntimeException("Email mismatch with existing account."); // 예외 처리 필요
            }
            user = updateUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserInfo, authentication);
        }
        return user;
    }

    private User registerNewUser(codepirate.tubelensbe.auth.oauth2.user.GoogleOAuth2UserInfo oAuth2UserInfo, Authentication authentication) {
        User user = new User();
        user.setGoogleId(oAuth2UserInfo.getId());
        user.setName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setPicture(oAuth2UserInfo.getPictureUrl());
        user.setAuthority(codepirate.tubelensbe.auth.common.Authority.ROLE_USER);
        user.setHire_date(new java.util.Date());

        user = userRepository.save(user);

        // 인증 토큰 가져오기
        String accessToken = null;
        String refreshToken = null;

        // OAuth2AuthenticationToken일 경우의 처리
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
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
        // UsernamePasswordAuthenticationToken일 경우의 처리 (JWT 로그인)
        else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            // JWT 로그인의 경우 이미 인증이 완료되었으므로 현재 컨텍스트에서 토큰을 가져옴
            accessToken = SecurityContextHolder.getContext().getAuthentication().getCredentials() != null ?
                    SecurityContextHolder.getContext().getAuthentication().getCredentials().toString() : null;
        }

        // 채널 ID 가져오기 및 설정
        if (accessToken != null) {
            try {
                String channelId = userService.fetchMyChannelId(accessToken, refreshToken);
                if (channelId != null && !channelId.isEmpty()) {
                    user.setChannel_id(channelId);
                    user = userRepository.save(user);
                }
            } catch (Exception e) {
                System.err.println("YouTube 채널 ID 가져오기 실패: " + e.getMessage());
            }
        }

        return user;
    }

    private User updateUser(User existingUser, codepirate.tubelensbe.auth.oauth2.user.GoogleOAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        existingUser.setPicture(oAuth2UserInfo.getPictureUrl());
        return userRepository.save(existingUser);
    }
}