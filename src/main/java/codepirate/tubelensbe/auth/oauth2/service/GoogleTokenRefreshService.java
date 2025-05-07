package codepirate.tubelensbe.auth.oauth2.service;

import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleTokenRefreshService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Transactional
    public void refreshGoogleToken(User user) {
        if (user.getGoogleRefreshToken() == null) {
            throw new RuntimeException("Google Refresh Token이 없습니다. 다시 로그인해주세요.");
        }

        String url = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", user.getGoogleRefreshToken());
        params.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.get("access_token") != null) {
                String newAccessToken = (String) responseBody.get("access_token");
                Integer expiresIn = (Integer) responseBody.get("expires_in");

                user.setGoogleAccessToken(newAccessToken);
                user.setGoogleTokenExpiryDate(Instant.now().plusSeconds(expiresIn));
                userRepository.save(user);
            }
        } catch (Exception e) {
            throw new RuntimeException("Google Token 갱신 실패: " + e.getMessage(), e);
        }
    }
}