package codepirate.tubelensbe.video.service;

import codepirate.tubelensbe.video.controller.TrendingVideoController;
import codepirate.tubelensbe.video.domain.ESVideo;
import codepirate.tubelensbe.video.domain.TrendingVideo;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
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

    public List<TrendingVideo> recommVideos(List<String> id) throws IOException {
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
        List<ESVideo> ESVideoList =  trendingVideoESRepository.recommendVideosByTitleVectors(videoid);

        List<TrendingVideo> videoList = new ArrayList<>();
        for (ESVideo esVideo : ESVideoList) {
            TrendingVideo v = new TrendingVideo();

            v.setId(esVideo.getId());
            v.setTitle(esVideo.getTitle());
            v.setThumbnails(esVideo.getThumbnails());
            v.setEmbedHtml(esVideo.getEmbedHtml());
            v.setPublishedAt(OffsetDateTime.parse(esVideo.getPublisedAt()));
            v.setDescription(esVideo.getDescription());
            v.setChannelTitle(esVideo.getChannelTitle());
            v.setViewCount(esVideo.getViewCount());
            v.setLikeCount(esVideo.getLikeCount());
            v.setCommentCount(esVideo.getCommentCount());

            videoList.add(v);
        }

        return videoList;
    }
}