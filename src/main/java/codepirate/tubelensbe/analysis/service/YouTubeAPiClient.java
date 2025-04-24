package codepirate.tubelensbe.analysis.service;


import codepirate.tubelensbe.analysis.Exception.YouTubeApiException;
import codepirate.tubelensbe.analysis.config.GoogleOAuthProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class YouTubeAPiClient {

    private final GoogleOAuthProperties googleOAuthProperties;

    public YouTubeAPiClient(GoogleOAuthProperties googleOAuthProperties) {
        this.googleOAuthProperties = googleOAuthProperties;
    }

    public List<String> fetchAllTags(String accessToken,String refreshToken) throws GeneralSecurityException, IOException {
        List<String> allTags = new ArrayList<>();

        try {
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(JacksonFactory.getDefaultInstance())
                    .setClientSecrets(googleOAuthProperties.getClientId(), googleOAuthProperties.getClientSecret())
                    .build()
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken);


            YouTube youtube = new YouTube.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                    .setApplicationName("YourApplicationName")
                    .build();


            YouTube.Channels.List channelRequest = youtube.channels().list(List.of("id"));
            channelRequest.setMine(true);
            channelRequest.setOauthToken(accessToken);

            ChannelListResponse channelResponse = channelRequest.execute();

            if (channelResponse.getItems().isEmpty()) return allTags;

            String myChannelId = channelResponse.getItems().get(0).getId();

            YouTube.Search.List searchRequest = youtube.search().list(List.of("snippet"));
            searchRequest.setChannelId(myChannelId);
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
                    if(video.getSnippet().getTags() != null) {
                        allTags.addAll(video.getSnippet().getTags());
                    }
                }
            }
        } catch (IOException e) {
            log.error("유튜브 API 호출 중 오류 발생", e);
            throw new YouTubeApiException("유튜브 API 오류: 태그 정보를 불러올 수 없습니다.", e);
        }
        return allTags;
    }
}
