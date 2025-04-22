package codepirate.tubelensbe.video.repository.youtube;

import codepirate.tubelensbe.video.dto.YouTubeCommentResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@Component
@HttpExchange("https://www.googleapis.com/youtube/v3")
public interface YouTubeCommentsRepository {

    @GetExchange("/commentThreads")
    YouTubeCommentResponse getComments(
            @RequestParam String part,
            @RequestParam String videoId,
            @RequestParam String key
    );
}
