package codepirate.tubelensbe.analytics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleAnalyticsServiceImpl implements GoogleAnalyticsService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    // TODO: Google Analytics Access Token 및 Refresh Token을 저장할 Repository 또는 Service Bean 주입
    // @Autowired
    // private GoogleAnalyticsTokenRepository googleAnalyticsTokenRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> getGoogleAnalyticsAccessToken(String authorizationCode, String redirectUri) {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("code", authorizationCode);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(tokenUrl, entity, Map.class);
            return responseEntity.getBody();
        } catch (Exception error) {
            System.err.println("Error during Google Access Token retrieval: " + error.getMessage());
            return null; // 또는 예외 처리
        }
    }

    @Override
    public void saveGoogleAnalyticsTokens(String userId, String accessToken, String refreshToken, Long expiresIn) {
        // TODO: userId를 기반으로 Google Analytics 토큰 정보를 저장하는 로직 구현
        // 예: googleAnalyticsTokenRepository.save(new GoogleAnalyticsToken(userId, accessToken, refreshToken, expiresIn, ...));
        System.out.println("Saving Google Analytics Tokens for User ID: " + userId);
        System.out.println("Google Access Token: " + accessToken);
        System.out.println("Google Refresh Token: " + refreshToken);
        System.out.println("Expires In: " + expiresIn);
    }

    // 필요하다면 Google Analytics API 호출 관련 메서드 구현
    /*
    @Override
    public Map<String, Object> getAnalyticsData(String accessToken, ...) {
        // RestTemplate 또는 WebClient를 사용하여 Google Analytics API 호출
        // Authorization 헤더에 accessToken 포함
        // ...
    }
    */
}