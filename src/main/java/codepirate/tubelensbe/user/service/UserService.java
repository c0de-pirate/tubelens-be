// user/service/UserService.java
package codepirate.tubelensbe.user.service;

import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

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

    public String fetchMyChannelId(String accessToken, String refreshToken){

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
        } catch (IOException | GeneralSecurityException e){
            e.printStackTrace();
        }
        return channelResponse.getItems().get(0).getId();
    }
}