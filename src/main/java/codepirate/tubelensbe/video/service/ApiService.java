package codepirate.tubelensbe.video.service;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import codepirate.tubelensbe.video.dto.VideoParam;
import codepirate.tubelensbe.video.repository.TrendingVideoRepository;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ApiService {
    private static final Logger log = LoggerFactory.getLogger(ApiService.class);
    private final TrendingVideoRepository trendingVideoRepository;

    public ApiService(TrendingVideoRepository trendingVideoRepository) {
        this.trendingVideoRepository = trendingVideoRepository;
    }

    public void insertVideos(VideoParam param) throws IOException {
//         JSON 데이터를 처리하기 위한 JsonFactory 객체 생성
        JsonFactory jsonFactory = new JacksonFactory();

        // YouTube 객체를 빌드하여 API에 접근할 수 있는 YouTube 클라이언트 생성
        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                jsonFactory,
                request -> {})
                .setApplicationName("tubelens")
                .build();

        // YouTube Search API를 사용하여 동영상 검색을 위한 요청 객체 생성
        YouTube.Videos.List video = youtube.videos().list(Collections.singletonList("snippet,player,statistics"));
        // API 키 설정
        video.set("chart", param.getChart());
        video.set("regionCode", param.getRegionCode());
        video.set("videoCategoryId", param.getVideoCategoryId());
        video.set("maxResults", param.getMaxResults());
        video.set("key", param.getKey());

        // 검색 요청 실행 및 응답 받아오기
        VideoListResponse videoListResponse = video.execute();

        if (videoListResponse.get("error") != null) {
            return;
        }

        // 검색 결과에서 동영상 목록 가져오기
        List<Video> videoResponseList = videoListResponse.getItems();

        List<TrendingVideo> trendingVideoList = new ArrayList<>();
        log.info(String.valueOf(videoResponseList.size()));
        for (Video v : videoResponseList) {
            TrendingVideo trendingVideo = new TrendingVideo();

            //<iframe> 제외, 링크만 저장
            String[] iframe = v.getPlayer().getEmbedHtml().split(" ");
            String link = iframe[3].split("\"")[1];
            String src = link.substring(2, link.length());

            trendingVideo.setId(v.getId());
            trendingVideo.setTitle(v.getSnippet().getTitle());
            trendingVideo.setThumbnails(v.getSnippet().getThumbnails().getMedium().getUrl());
            trendingVideo.setEmbedHtml(src);
            trendingVideo.setPublishedAt(v.getSnippet().getPublishedAt());
            trendingVideo.setDescription(v.getSnippet().getDescription());
            trendingVideo.setChannelTitle(v.getSnippet().getChannelTitle());
            trendingVideo.setViewCount(v.getStatistics().getViewCount());
            trendingVideo.setLikeCount(v.getStatistics().getLikeCount());
            trendingVideo.setCommentCount(v.getStatistics().getCommentCount());
            trendingVideo.setTags(v.getSnippet().getTags());

            trendingVideoList.add(trendingVideo);
        }

        trendingVideoRepository.saveAll(trendingVideoList);
    }
}
