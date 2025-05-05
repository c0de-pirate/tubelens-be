package codepirate.tubelensbe.user.service;

import codepirate.tubelensbe.TubelensBeApplication;
import codepirate.tubelensbe.auth.common.Authority;
import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.user.exception.YouTubeApiException;
import codepirate.tubelensbe.user.repository.UserRepository;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final static Logger Log = LoggerFactory.getLogger(TubelensBeApplication.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Map<String, Object> processOAuth2User(OAuth2AuthenticationToken oauthToken, OAuth2User oauth2User) {
        String googleId = oauth2User.getAttribute("sub");
        OAuth2TokenInfo tokenInfo = getOAuth2TokenInfo(oauthToken);

        return userRepository.findByGoogleId(googleId)
                .map(user -> processExistingUser(user, tokenInfo))
                .orElseGet(() -> processNewUser(oauth2User, tokenInfo));
    }

    public Map<String, Object> processJwtUser(UserDetails userDetails) {
        String username = userDetails.getUsername();

        Optional<User> userOptional = userRepository.findByName(username);

        if (userOptional.isPresent()) {
            return convertUserToMap(userOptional.get());
        }

        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("name", username);
        return basicInfo;
    }

    //youtube api v3 호출
    private Map<String, Object> processExistingUser(User user, OAuth2TokenInfo tokenInfo) {
        fetchAndSetChannelId(user, tokenInfo);

        if (user.getHire_date() == null) {
            user.setHire_date(new java.util.Date());
            userRepository.save(user);
        }

        return convertUserToMap(user);
    }

    @Transactional
    private Map<String, Object> processNewUser(OAuth2User oauth2User, OAuth2TokenInfo tokenInfo) {
        String googleId = oauth2User.getAttribute("sub");

        User user = new User(
                googleId,
                oauth2User.getAttribute("name"),
                oauth2User.getAttribute("email"),
                oauth2User.getAttribute("picture"),
                Authority.ROLE_USER
        );

        fetchAndSetChannelId(user, tokenInfo);

        return convertUserToMap(user);
    }

    private OAuth2TokenInfo getOAuth2TokenInfo(OAuth2AuthenticationToken oauthToken) {
        String accessToken = null;
        String refreshToken = null;

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

        return new OAuth2TokenInfo(accessToken, refreshToken);
    }

    //유틸
    public Map<String, Object> convertUserToMap(User user) {
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

    private YouTube getYoutubeClient(String accessToken, String refreshToken) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        // OAuth2Credentials 사용
        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .build();

        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);

        return new YouTube.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("TubelensBeApplication")
                .build();
    }

    public String fetchMyChannelId(String accessToken, String refreshToken) {
        ChannelListResponse channelResponse = new ChannelListResponse();

        try {
            YouTube youtube = getYoutubeClient(accessToken, refreshToken);

            YouTube.Channels.List channelRequest = youtube.channels().list(List.of("id"));
            channelRequest.setMine(true);
            channelRequest.setOauthToken(accessToken);

            channelResponse = channelRequest.execute();
            if (channelResponse.getItems().isEmpty()) {
                throw new IllegalStateException("채널 ID를 찾을 수 없습니다.");
            }
        } catch (IOException e) {
            log.error("""
                    YouTube API 호출 중 오류가 발생했습니다:
                    메시지: {}
                    액세스 토큰: {}
                    """, e.getMessage(), accessToken);
            throw new YouTubeApiException("YouTube API에 접근할 수 없습니다", e);
        } catch (GeneralSecurityException e) {
            log.error("YouTube API 보안 오류: {}", e.getMessage(), e);
            throw new YouTubeApiException("YouTube API 보안 설정에 문제가 있습니다", e);
        }
        return channelResponse.getItems().get(0).getId();
    }

    @Async
    @Transactional
    public CompletableFuture<String> fetchChannelIdAsync(String accessToken, String refreshToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchMyChannelId(accessToken, refreshToken);
            } catch (Exception e) {
                log.error("채널 ID 조회 실패", e);
                return null;
            }
        }, Executors.newVirtualThreadPerTaskExecutor());
    }

    private void fetchAndSetChannelId(User user, OAuth2TokenInfo tokenInfo) {
        if ((user.getChannel_id() == null || user.getChannel_id().isEmpty()) && tokenInfo.accessToken != null) {
            try {
                // 비동기 호출
                CompletableFuture<String> channelIdFuture =
                        fetchChannelIdAsync(tokenInfo.accessToken, tokenInfo.refreshToken);

                // 비동기 결과 처리
                channelIdFuture.thenAccept(channelId -> {
                    if (channelId != null && !channelId.isEmpty()) {
                        user.setChannel_id(channelId);
                        userRepository.save(user);
                    }
                }).exceptionally(ex -> {
                    log.error("YouTube 채널 ID 가져오기 실패: " + ex.getMessage());
                    return null;
                });

                // 선택 사항: 결과를 기다리려면 (비동기의 장점이 줄어듦)
                // String channelId = channelIdFuture.get(5, TimeUnit.SECONDS);

            } catch (Exception e) {
                log.error("YouTube 채널 ID 가져오기 실패: " + e.getMessage());
            }
        }
    }


    private record OAuth2TokenInfo(String accessToken, String refreshToken) {
    }
}