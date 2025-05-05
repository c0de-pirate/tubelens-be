package codepirate.tubelensbe.funnel.Repository;

import codepirate.tubelensbe.funnel.dto.AnalyticsReportResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@Component
@HttpExchange("https://youtubeanalytics.googleapis.com/v2")
public interface FunnelYoutubeRepository {

    @GetExchange("/reports")
    AnalyticsReportResponse getFunnelResponse(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String ids,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String metrics,
            @RequestParam String dimensions,
            @RequestParam String sort
    );
}