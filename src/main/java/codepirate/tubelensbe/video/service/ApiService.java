package codepirate.tubelensbe.video.service;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import codepirate.tubelensbe.video.dto.VideoParam;
import codepirate.tubelensbe.video.repository.TrendingVideoRepositoryCustom;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ApiService {
    private static final Logger log = LoggerFactory.getLogger(ApiService.class);
    private final TrendingVideoRepositoryCustom trendingVideoRepositoryCustom;

    public ApiService(TrendingVideoRepositoryCustom trendingVideoRepositoryCustom) {
        this.trendingVideoRepositoryCustom = trendingVideoRepositoryCustom;
    }

    public void insertVideos(VideoParam param) throws IOException {

        JsonFactory jsonFactory = new JacksonFactory();

        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                jsonFactory,
                request -> {})
                .setApplicationName("tubelens")
                .build();

        YouTube.Videos.List video = youtube.videos().list(Collections.singletonList("snippet,player,statistics"));

        video.set("chart", param.getChart());
        video.set("regionCode", param.getRegionCode());
        video.set("videoCategoryId", param.getVideoCategoryId());
        video.set("maxResults", param.getMaxResults());
        video.set("key", param.getKey());

        VideoListResponse videoListResponse = video.execute();

        if (videoListResponse.get("error") != null) {
            return;
        }

        List<Video> videoResponseList = videoListResponse.getItems();

        List<TrendingVideo> trendingVideoList = new ArrayList<>();
        for (Video v : videoResponseList) {
            String[] iframe = v.getPlayer().getEmbedHtml().split(" ");
            String link = iframe[3].split("\"")[1];
            String src = link.substring(2, link.length());

            OffsetDateTime publishedAt = OffsetDateTime.parse(v.getSnippet().getPublishedAt().toString());

            Long viewCount = (v.getStatistics().getViewCount() != null) ?
                    v.getStatistics().getViewCount().longValue() : 0L;
            Long likeCount = (v.getStatistics().getLikeCount() != null) ?
                    v.getStatistics().getLikeCount().longValue() : 0L;
            Long commentCount = (v.getStatistics().getCommentCount() != null) ?
                    v.getStatistics().getCommentCount().longValue() : 0L;

            TrendingVideo trendingVideo = TrendingVideo.builder()
                    .id(v.getId())
                    .title(v.getSnippet().getTitle())
                    .thumbnails(v.getSnippet().getThumbnails().getMedium().getUrl())
                    .embedHtml(src)
                    .publishedAt(publishedAt)
                    .description(v.getSnippet().getDescription())
                    .channelTitle(v.getSnippet().getChannelTitle())
                    .viewCount(viewCount)
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .tags(v.getSnippet().getTags())
                    .build();

            trendingVideoList.add(trendingVideo);
        }

        trendingVideoRepositoryCustom.batchInsertIgnore(trendingVideoList);
    }
}
