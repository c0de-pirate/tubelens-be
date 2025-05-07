package codepirate.tubelensbe.auth.oauth2.service;

import codepirate.tubelensbe.auth.common.Authority;
import codepirate.tubelensbe.auth.oauth2.user.GoogleOAuth2UserInfo;
import codepirate.tubelensbe.auth.refreshtoken.TokenExtractor;
import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OAuth2UserProcessingService {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final TokenExtractor tokenExtractor;

    @Transactional
    public User processOAuth2User(OAuth2User oAuth2User) {
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

        // 사용자 찾기 또는 생성
        User user = userRepository.findByGoogleId(userInfo.getId())
                .orElseGet(() -> createNewUser(userInfo));

        // 채널 ID 업데이트
        if (user.getChannelId() == null || user.getChannelId().isEmpty()) {
            // 채널 ID는 나중에 따로 설정됨 (UserService의 fetchAndSetChannelId 메소드)
        }

        // 토큰 저장 (현재 인증 정보에서 추출)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            saveTokens(user, authentication);
        }

        return userRepository.save(user);
    }

    private void saveTokens(User user, Authentication authentication) {
        String accessToken = tokenExtractor.extractAccessToken(authentication);
        String refreshToken = tokenExtractor.extractRefreshToken(authentication);

        if (accessToken != null) {
            user.setGoogleAccessToken(accessToken);
            // 일반적인 Google 액세스 토큰 만료 시간은 1시간
            user.setGoogleTokenExpiryDate(Instant.now().plusSeconds(3600));
        }

        if (refreshToken != null) {
            user.setGoogleRefreshToken(refreshToken);
        }
    }

    private User updateExistingUser(User user, GoogleOAuth2UserInfo userInfo) {
        if (!user.getEmail().equals(userInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email mismatch with existing account");
        }

        user.setName(userInfo.getName());
        user.setPicture(userInfo.getPictureUrl());
        return userRepository.save(user);
    }

    private User createNewUser(GoogleOAuth2UserInfo userInfo) {
        User user = new User();
        user.setGoogleId(userInfo.getId());
        user.setName(userInfo.getName());
        user.setEmail(userInfo.getEmail());
        user.setPicture(userInfo.getPictureUrl());
        user.setAuthority(Authority.ROLE_USER);
        user.setHire_date(new java.util.Date());
        return userRepository.save(user);
    }
}