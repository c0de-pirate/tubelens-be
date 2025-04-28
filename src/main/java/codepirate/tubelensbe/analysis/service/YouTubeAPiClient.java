package codepirate.tubelensbe.analysis.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
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

    private YouTube getYoutubeClient(String accessToken, String refreshToken) throws GeneralSecurityException, IOException {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JacksonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);


        return new YouTube.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                .setApplicationName("YourApplicationName")
                .build();
    }

    public List<String> fetchAllTags(String channelId, String accessToken, String refreshToken) throws GeneralSecurityException, IOException {
        List<String> allTags = new ArrayList<>();
        YouTube youtube = getYoutubeClient(accessToken, refreshToken);

        YouTube.Search.List searchRequest = youtube.search().list(List.of("snippet"));
        searchRequest.setChannelId(channelId);
        searchRequest.setType(List.of("video"));
        searchRequest.setMaxResults(50L);
        searchRequest.setOauthToken(accessToken);

        SearchListResponse searchListResponse = searchRequest.execute();

        for (SearchResult searchResult : searchListResponse.getItems()) {
            String videoId = searchResult.getId().getVideoId();

            YouTube.Videos.List videoRequest = youtube.videos().list(List.of("snippet"));
            videoRequest.setId(List.of(videoId));
            videoRequest.setOauthToken(accessToken);

            VideoListResponse videoListResponse = videoRequest.execute();

            for (Video video : videoListResponse.getItems()) {
                if (video.getSnippet().getTags() != null) {
                    allTags.addAll(video.getSnippet().getTags());
                }
            }
        }
        return allTags;
    }
}
