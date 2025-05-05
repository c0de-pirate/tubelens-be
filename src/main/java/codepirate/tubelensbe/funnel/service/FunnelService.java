package codepirate.tubelensbe.funnel.service;

import codepirate.tubelensbe.funnel.Repository.FunnelYoutubeRepository;
import codepirate.tubelensbe.funnel.dto.AnalyticsReportResponse;
import codepirate.tubelensbe.funnel.dto.FunnelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FunnelService {

    private final FunnelYoutubeRepository funnelYoutubeRepository;

    public FunnelResponse getFunnel(String token) {
        AnalyticsReportResponse funnel = funnelYoutubeRepository.getFunnelResponse(
                token,
                "channel==MINE",
                LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                "views,estimatedMinutesWatched",
                "insightTrafficSourceType",
                "-views"
        );

        FunnelResponse.FunnelResponseBuilder builder = FunnelResponse.builder();

        if (funnel.getRows() != null) {
            for (List<Object> row : funnel.getRows()) {
                if (row.size() >= 2) {
                    String type = row.get(0).toString();
                    long views = Long.parseLong(row.get(1).toString());

                    switch (type) {
                        case "EXT_URL":
                            builder.EXT_URL(views);
                            break;
                        case "YT_SEARCH":
                            builder.YT_SEARCH(views);
                            break;
                        case "RELATED_VIDEO":
                            builder.RELATED_VIDEO(views);
                            break;
                        case "PLAYLIST":
                            builder.PLAYLIST(views);
                            break;
                        case "SUBSCRIBER":
                            builder.SUBSCRIBER(views);
                            break;
                        case "CHANNEL":
                            builder.CHANNEL(views);
                            break;
                        case "NOTIFICATION":
                            builder.NOTIFICATION(views);
                            break;
                        case "ADVERTISING":
                            builder.ADVERTISING(views);
                            break;
                        default:
                            builder.etc(views); // 기타 유입경로
                            break;
                    }
                }
            }
        }

        return builder.build();
    }
}