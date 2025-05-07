package codepirate.tubelensbe.analysis.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.zaxxer.hikari.SQLExceptionOverride;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeAPiClient {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

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


    public List<String> fetchAllTags(String channelId) throws GeneralSecurityException, IOException {
        List<String> allTags = new ArrayList<>();
        JsonFactory jsonFactory = new JacksonFactory();

        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                jsonFactory,
                request -> {})
                .setApplicationName("TubelensBeApplication")
                .build();

        YouTube.Search.List searchRequest = youtube.search().list(List.of("snippet"));
        searchRequest.setChannelId(channelId);
        searchRequest.setType(List.of("video"));
        searchRequest.setMaxResults(50L);
        searchRequest.setKey(youtubeApiKey);
        //searchRequest.setOauthToken(accessToken);

        SearchListResponse searchListResponse = new SearchListResponse();
        try {
            searchListResponse = searchRequest.execute();
        } catch (Exception e) {
            log.error("YouTube API 호출 실패: {}", e.getMessage(), e);
            return allTags; // 빈 리스트 반환
        }

        // 여기에 null 체크 추가
        List<SearchResult> items = searchListResponse.getItems();
        if (items == null) {
            log.warn("YouTube API 응답에 items가 null입니다. channelId: {}", channelId);
            return allTags; // 빈 리스트 반환
        }

        for (SearchResult searchResult : items) {
            // null 체크 추가
            if (searchResult.getId() == null || searchResult.getId().getVideoId() == null) {
                continue; // 이 항목 건너뛰기
            }

            String videoId = searchResult.getId().getVideoId();

            try {
                YouTube.Videos.List videoRequest = youtube.videos().list(List.of("snippet"));
                videoRequest.setId(List.of(videoId));
                videoRequest.setKey(youtubeApiKey);

                VideoListResponse videoListResponse = videoRequest.execute();

                // null 체크 추가
                if (videoListResponse.getItems() == null) {
                    continue;
                }

                for (Video video : videoListResponse.getItems()) {
                    // null 체크 추가
                    if (video.getSnippet() != null && video.getSnippet().getTags() != null) {
                        allTags.addAll(video.getSnippet().getTags());
                    }
                }
            } catch (Exception e) {
                log.error("비디오 정보 가져오기 실패 (videoId: {}): {}", videoId, e.getMessage());
                // 한 비디오에서 오류가 발생해도 계속 진행
            }
        }
        return allTags;
    }
}
