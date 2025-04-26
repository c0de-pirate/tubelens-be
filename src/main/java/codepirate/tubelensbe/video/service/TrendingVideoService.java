package codepirate.tubelensbe.video.service;

import codepirate.tubelensbe.video.controller.TrendingVideoController;
import codepirate.tubelensbe.video.domain.ESVideo;
import codepirate.tubelensbe.video.repository.TrendingVideoESRepository;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class TrendingVideoService {
    private final TrendingVideoESRepository trendingVideoESRepository;

    private static final Logger log = LoggerFactory.getLogger(TrendingVideoController.class);

    public TrendingVideoService(TrendingVideoESRepository trendingVideoESRepository) {
        this.trendingVideoESRepository = trendingVideoESRepository;
    }

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    public List<ESVideo> recommVideos(List<String> id) throws IOException {
        //제목 추출 후 repository로
        JsonFactory jsonFactory = new JacksonFactory();

        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                jsonFactory,
                request -> {})
                .setApplicationName("tubelens")
                .build();

        YouTube.Videos.List video = youtube.videos().list(Collections.singletonList("snippet,statistics"));

        video.setId(id);
        video.setKey(youtubeApiKey);

        VideoListResponse videoListResponse = video.execute();
        String videoid = videoListResponse.getItems().get(0).getId();
        return trendingVideoESRepository.recommendVideosByTitleVectors(videoid);
    }
}