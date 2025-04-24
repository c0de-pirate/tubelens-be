package codepirate.tubelensbe.video.scheduler;

import codepirate.tubelensbe.video.dto.VideoParam;
import codepirate.tubelensbe.video.service.ApiService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.List;

@Component
public class ApiCallJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);
    private final ApiService apiService;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    public ApiCallJob(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        List<String> categoryId = List.of(
                "1", "2", "10", "15", "17", "18", "19", "20", "21", "22", "23", "24",
                "25", "26", "27", "28", "30", "31", "32", "33", "34", "35", "36",
                "37", "38", "39", "40", "41", "42", "43", "44"
        );

        for (String id : categoryId) {
            VideoParam videoParam = new VideoParam(
                    "snippet,player,statistics",
                    "mostPopular",
                    "KR",
                    id,
                    50L,
                    youtubeApiKey
            );

            try {
                apiService.insertVideos(videoParam);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    continue;
                }
            } catch(IOException e) {
                continue;
            }
        }
    }

}
