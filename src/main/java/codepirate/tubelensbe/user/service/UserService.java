package codepirate.tubelensbe.user.service;

import codepirate.tubelensbe.auth.common.Authority;
import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    public UserService(UserRepository userRepository, OAuth2AuthorizedClientService authorizedClientService) {
        this.userRepository = userRepository;
        this.authorizedClientService = authorizedClientService;
    }

    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Map<String, Object> processOAuth2User(OAuth2AuthenticationToken oauthToken, OAuth2User oauth2User) {
        String googleId = oauth2User.getAttribute("sub");

        OAuth2TokenInfo tokenInfo = getOAuth2TokenInfo(oauthToken);

        Optional<User> userOptional = userRepository.findByGoogleId(googleId);

        if (userOptional.isPresent()) {
            return processExistingUser(userOptional.get(), tokenInfo);
        } else {
            return processNewUser(oauth2User, tokenInfo);
        }
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
        if ((user.getChannel_id() == null || user.getChannel_id().isEmpty()) && tokenInfo.accessToken != null) {
            try {
                String channelId = fetchMyChannelId(tokenInfo.accessToken, tokenInfo.refreshToken);
                if (channelId != null && !channelId.isEmpty()) {
                    user.setChannel_id(channelId);
                    userRepository.save(user);
                }
            } catch (Exception e) {
                System.err.println("YouTube 채널 ID 가져오기 실패: " + e.getMessage());
            }
        }

        if (user.getHire_date() == null) {
            user.setHire_date(new java.util.Date());
            userRepository.save(user);
        }

        return convertUserToMap(user);
    }

    private Map<String, Object> processNewUser(OAuth2User oauth2User, OAuth2TokenInfo tokenInfo) {
        String googleId = oauth2User.getAttribute("sub");

        User user = new User(
                googleId,
                oauth2User.getAttribute("name"),
                oauth2User.getAttribute("email"),
                oauth2User.getAttribute("picture"),
                Authority.ROLE_USER
        );

        user = userRepository.save(user);

        // 액세스 토큰이 있으면 YouTube API 호출하여 채널 ID 가져오기
        if (tokenInfo.accessToken != null) {
            try {
                String channelId = fetchMyChannelId(tokenInfo.accessToken, tokenInfo.refreshToken);
                if (channelId != null && !channelId.isEmpty()) {
                    user.setChannel_id(channelId);
                    userRepository.save(user);
                }
            } catch (Exception e) {
                System.err.println("YouTube 채널 ID 가져오기 실패: " + e.getMessage());
            }
        }

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
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JacksonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);

        return new YouTube.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
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
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return channelResponse.getItems().get(0).getId();
    }

    private static class OAuth2TokenInfo {
        final String accessToken;
        final String refreshToken;

        OAuth2TokenInfo(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}