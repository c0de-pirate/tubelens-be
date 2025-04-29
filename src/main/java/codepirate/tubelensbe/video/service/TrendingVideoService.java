package codepirate.tubelensbe.video.service;

import codepirate.tubelensbe.video.controller.TrendingVideoController;
import codepirate.tubelensbe.video.domain.ESVideo;
import codepirate.tubelensbe.video.domain.TrendingVideo;
import codepirate.tubelensbe.video.repository.elasticsearch.TrendingVideoESRepository;
import codepirate.tubelensbe.video.repository.TrendingVideoRepositoryCustom;
import codepirate.tubelensbe.video.repository.youtube.TrendingVideoRepository;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TrendingVideoService {
    private final TrendingVideoESRepository trendingVideoESRepository;
    private final TrendingVideoRepository trendingVideoRepository;

    private static final Logger log = LoggerFactory.getLogger(TrendingVideoController.class);

    public TrendingVideoService(TrendingVideoESRepository trendingVideoESRepository, TrendingVideoRepositoryCustom trendingVideoRepositoryCustom, TrendingVideoRepository trendingVideoRepository) {
        this.trendingVideoESRepository = trendingVideoESRepository;
        this.trendingVideoRepository = trendingVideoRepository;
    }

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    public Optional<TrendingVideo> getVideo(List<String> id) {
        Optional<TrendingVideo> video = trendingVideoRepository.findById(id.get(0));
        if (video.isPresent()) {
            return video;
        }
        return null;
    }

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

    // 날짜 제한 없이 조회수 기준 상위 N개
    public List<TrendingVideo> getTopVideosByViews(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return trendingVideoRepository.findByOrderByViewCountDesc(pageable);

    }

    // 날짜 제한 없이 좋아요 기준 상위 N개
    public List<TrendingVideo> getTopVideosByLikes(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return trendingVideoRepository.findByOrderByLikeCountDesc(pageable);
    }

    // 오늘 기준 조회수 상위 N개
    public List<TrendingVideo> getTodayTopVideosByViews(int limit) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        Pageable pageable = PageRequest.of(0, limit);
        return trendingVideoRepository.findByUpdated_atBetweenOrderByViewCountDesc(
                startOfDay, endOfDay, pageable);
    }

    // 오늘 기준 좋아요 상위 N개
    public List<TrendingVideo> getTodayTopVideosByLikes(int limit) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        Pageable pageable = PageRequest.of(0, limit);
        return trendingVideoRepository.findByUpdated_atBetweenOrderByLikeCountDesc(
                startOfDay, endOfDay, pageable);
    }

    // 특정 기간 내 조회수 상위 N개
    public List<TrendingVideo> getTopVideosByViewsInPeriod(
            LocalDateTime startDate, LocalDateTime endDate, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return trendingVideoRepository.findByUpdated_atBetweenOrderByViewCountDesc(
                startDate, endDate, pageable);
    }

    // 특정 기간 내 좋아요 상위 N개
    public List<TrendingVideo> getTopVideosByLikesInPeriod(
            LocalDateTime startDate, LocalDateTime endDate, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return trendingVideoRepository.findByUpdated_atBetweenOrderByLikeCountDesc(
                startDate, endDate, pageable);
    }
}