package codepirate.tubelensbe.video.service;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import codepirate.tubelensbe.video.dto.VideoParam;
import codepirate.tubelensbe.video.repository.TrendingVideoRepository;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ApiService {
    private static final Logger log = LoggerFactory.getLogger(ApiService.class);
    private final TrendingVideoRepository trendingVideoRepository;

    @Value("${youtube.api.key}")
    private String apiKey;


    public ApiService(TrendingVideoRepository trendingVideoRepository) {
        this.trendingVideoRepository = trendingVideoRepository;
    }

    public void insertVideos(VideoParam param) throws IOException {
        log.info("service");
        JsonFactory jsonFactory = new JacksonFactory();

        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                jsonFactory,
                request -> {})
                .build();

        YouTube.Videos.List video = youtube.videos().list(Collections.singletonList("snippet,player,statistics"));
        video.set("chart", param.getChart());
        video.set("regionCode", param.getRegionCode());
        video.set("videoCategoryId", param.getVideoCategoryId());
        video.set("maxResults", param.getMaxResults());
        video.set("key", apiKey);

        VideoListResponse videoListResponse = video.execute();
        List<Video> videoResponseList = videoListResponse.getItems();

        List<TrendingVideo> trendingVideoList = new ArrayList<>();
        log.info(String.valueOf(videoResponseList.size()));
        for (Video v : videoResponseList) {
            TrendingVideo trendingVideo = new TrendingVideo();

            String[] iframe = v.getPlayer().getEmbedHtml().split(" ");
            String link = iframe[3].split("\"")[1];
            String src = link.substring(2);

            trendingVideo.setId(v.getId());
            trendingVideo.setTitle(v.getSnippet().getTitle());
            trendingVideo.setThumbnails(v.getSnippet().getThumbnails().getMedium().getUrl());
            trendingVideo.setEmbedHtml(src);
            trendingVideo.setDescription(v.getSnippet().getDescription());
            trendingVideo.setChannelTitle(v.getSnippet().getChannelTitle());
            trendingVideo.setViewCount(String.valueOf(v.getStatistics().getViewCount()));
            trendingVideo.setLikeCount(String.valueOf(v.getStatistics().getLikeCount()));
            trendingVideo.setCommentCount(String.valueOf(v.getStatistics().getCommentCount()));
            trendingVideo.setTags(v.getSnippet().getTags());

            // DateTime을 OffsetDateTime으로 변환
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(v.getSnippet().getPublishedAt().toString());

            // publishedAt에 OffsetDateTime을 설정
            trendingVideo.setPublishedAt(offsetDateTime);


            trendingVideoList.add(trendingVideo);
        }

        trendingVideoRepository.saveAll(trendingVideoList);
    }
}
