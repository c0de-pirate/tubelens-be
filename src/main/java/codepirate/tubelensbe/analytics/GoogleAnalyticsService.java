package codepirate.tubelensbe.analytics;

import java.util.Map;

public interface GoogleAnalyticsService {

    Map<String, Object> getGoogleAnalyticsAccessToken(String authorizationCode, String redirectUri);

    void saveGoogleAnalyticsTokens(String userId, String accessToken, String refreshToken, Long expiresIn);

    // 필요하다면 Google Analytics API 호출 관련 메서드 추가
    // Map<String, Object> getAnalyticsData(String accessToken, ...);
}