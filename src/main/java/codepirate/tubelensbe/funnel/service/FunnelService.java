package codepirate.tubelensbe.funnel.service;

import codepirate.tubelensbe.auth.jwt.JwtTokenProvider;
import codepirate.tubelensbe.auth.oauth2.service.GoogleTokenRefreshService;
import codepirate.tubelensbe.funnel.Repository.FunnelYoutubeRepository;
import codepirate.tubelensbe.funnel.dto.AnalyticsReportResponse;
import codepirate.tubelensbe.funnel.dto.FunnelResponse;
import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FunnelService {

    private final FunnelYoutubeRepository funnelYoutubeRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenRefreshService googleTokenRefreshService;

    public FunnelResponse getFunnel(String jwtToken) {
        try {
            // JWT 토큰에서 사용자 이름 추출
            String token = jwtToken.replace("Bearer ", "");
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // 사용자 정보 찾기
            User user = userRepository.findByName(username)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            String channelId = user.getChannelId();
            if (channelId == null || channelId.isEmpty()) {
                throw new RuntimeException("채널 ID가 없습니다. 다시 로그인해주세요.");
            }

            // Google OAuth 토큰이 만료된 경우 갱신
            if (user.getGoogleTokenExpiryDate() != null &&
                    user.getGoogleTokenExpiryDate().isBefore(Instant.now())) {
                googleTokenRefreshService.refreshGoogleToken(user);
            }

            // Google OAuth 토큰으로 YouTube Analytics API 호출
            String googleOAuthToken = "Bearer " + user.getGoogleAccessToken();

            // 요청 파라미터 로깅
            log.info("YouTube Analytics API 요청 파라미터:");
            log.info("- 인증 토큰: {}", googleOAuthToken.substring(0, 15) + "...");
            log.info("- 채널 ID: {}", channelId);
            log.info("- 시작일: {}", LocalDate.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            log.info("- 종료일: {}", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            AnalyticsReportResponse funnel = funnelYoutubeRepository.getFunnelResponse(
                    googleOAuthToken,
                    "channel==" + channelId,
                    LocalDate.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    "views,estimatedMinutesWatched",
                    "insightTrafficSourceType",
                    "-views"
            );

            // 응답 로깅
            log.info("YouTube Analytics API 응답: {}", funnel);

            // 응답 구조 상세 로깅
            if (funnel != null) {
                log.info("응답 종류(kind): {}", funnel.getKind());

                if (funnel.getColumnHeaders() != null) {
                    log.info("컬럼 헤더 수: {}", funnel.getColumnHeaders().size());
                    for (int i = 0; i < funnel.getColumnHeaders().size(); i++) {
                        AnalyticsReportResponse.ColumnHeader header = funnel.getColumnHeaders().get(i);
                        log.info("컬럼 #{}: 이름={}, 타입={}, 데이터타입={}",
                                i, header.getName(), header.getColumnType(), header.getDataType());
                    }
                } else {
                    log.info("컬럼 헤더 없음");
                }

                if (funnel.getRows() != null) {
                    log.info("데이터 행 수: {}", funnel.getRows().size());
                    for (int i = 0; i < funnel.getRows().size(); i++) {
                        List<Object> row = funnel.getRows().get(i);
                        log.info("행 #{}: {}", i, row);
                    }
                } else {
                    log.info("데이터 행 없음");
                }
            } else {
                log.error("YouTube Analytics API 응답이 null입니다");
            }

            // 이하 기존 코드 동일...
            FunnelResponse.FunnelResponseBuilder builder = FunnelResponse.builder();

            if (funnel.getRows() != null) {
                for (List<Object> row : funnel.getRows()) {
                    if (row.size() >= 2) {
                        String type = row.get(0).toString();
                        long views = Long.parseLong(row.get(1).toString());

                        log.info("행 처리: 유형={}, 조회수={}", type, views);

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
                            case "YT_CHANNEL":  // 이 부분 추가
                                builder.CHANNEL(views);
                                break;
                            case "NOTIFICATION":
                                builder.NOTIFICATION(views);
                                break;
                            case "ADVERTISING":
                                builder.ADVERTISING(views);
                                break;
                            default:
                                log.info("기타 유형 발견: {}", type);
                                builder.etc(views);
                                break;
                        }
                    }
                }
            }

            FunnelResponse response = builder.build();
            log.info("최종 응답: {}", response);
            return response;
        } catch (Exception e) {
            log.error("유튜브 퍼널 데이터 가져오기 실패: {}", e.getMessage(), e);
            // 오류 발생 시 빈 응답 반환
            return FunnelResponse.builder().build();
        }
    }
}