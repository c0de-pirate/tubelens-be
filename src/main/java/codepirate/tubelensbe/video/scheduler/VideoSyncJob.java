package codepirate.tubelensbe.video.scheduler;

import codepirate.tubelensbe.video.dto.VideoParam;
import codepirate.tubelensbe.video.service.ApiService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VideoSyncJob implements Job {

    private final ApiService apiService;

    @Value("${youtube.api.key}")
    private String apiKey;

    public VideoSyncJob(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 고정된 파라미터로 설정
        VideoParam param = new VideoParam(
                "snippet",           // part
                "mostPopular",       // chart
                "KR",                // regionCode
                "0",                 // videoCategoryId (all)
                20L,                 // maxResults
                apiKey               // API Key
        );

        try {
            apiService.insertVideos(param);
        } catch (IOException e) {
            throw new JobExecutionException("YouTube API 호출 실패", e);
        }
    }
}
