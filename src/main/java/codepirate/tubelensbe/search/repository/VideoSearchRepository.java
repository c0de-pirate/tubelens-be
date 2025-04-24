package codepirate.tubelensbe.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import codepirate.tubelensbe.search.dto.VideoSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import codepirate.tubelensbe.search.domain.SearchVideo;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Slf4j
@Repository
@RequiredArgsConstructor
public class VideoSearchRepository {

    private final ElasticsearchClient elasticsearchClient;

    public List<VideoSearchResult> searchByKeyword(String keyword, String fuzzinessLevel) {
        try {
            SearchResponse<SearchVideo> exactMatchResponse = elasticsearchClient.search(s -> s
                            .index("tubelens_video")
                            .query(q -> q.bool(b -> b
                                    .should(sh -> sh.matchPhrase(mp -> mp.field("title.ko").query(keyword)))
                                    .should(sh -> sh.matchPhrase(mp -> mp.field("title.en").query(keyword)))
                                    .minimumShouldMatch("1")
                            ))
                            .sort(so -> so.field(f -> f.field("viewCount").order(SortOrder.Desc))),
                    SearchVideo.class);

            SearchResponse<SearchVideo> fuzzyMatchResponse = elasticsearchClient.search(s -> s
                            .index("tubelens_video")
                            .query(q -> q.bool(b -> b
                                    .should(sh -> sh.match(m -> m.field("title.ko").query(keyword).fuzziness(fuzzinessLevel)))
                                    .should(sh -> sh.match(m -> m.field("title.en").query(keyword).fuzziness(fuzzinessLevel)))
                                    .minimumShouldMatch("1")
                            ))
                            .sort(so -> so.field(f -> f.field("viewCount").order(SortOrder.Desc))),
                    SearchVideo.class);

            Set<String> seenTitles = new HashSet<>();
            List<VideoSearchResult> results = new ArrayList<>();

            Consumer<SearchVideo> addIfNotDuplicate = v -> {
                if (v != null && v.getTitle() != null && seenTitles.add(v.getTitle())) {
                    results.add(mapToResult(v));
                }
            };

            exactMatchResponse.hits().hits().forEach(hit -> addIfNotDuplicate.accept(hit.source()));
            fuzzyMatchResponse.hits().hits().forEach(hit -> addIfNotDuplicate.accept(hit.source()));

            return results;

        } catch (IOException e) {
            log.error("Elasticsearch 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // 접두사 기반 검색
    public List<VideoSearchResult> searchByPrefix(String keyword) {
        try {
            SearchResponse<SearchVideo> response = elasticsearchClient.search(s -> s
                            .index("tubelens_video")
                            .query(q -> q.bool(b -> b
                                    .should(sh -> sh.prefix(p -> p.field("title.ko").value(keyword))).boost(2.0f)
                                    .should(sh -> sh.prefix(p -> p.field("title.en").value(keyword))).boost(2.0f)
                                    .minimumShouldMatch("1")
                            ))
                            .sort(so -> so.field(f -> f.field("viewCount").order(SortOrder.Desc))),
                    SearchVideo.class);

            Set<String> seenTitles = new HashSet<>();
            List<VideoSearchResult> results = new ArrayList<>();

            for (Hit<SearchVideo> hit : response.hits().hits()) {
                SearchVideo video = hit.source();
                if (video != null && video.getTitle() != null && seenTitles.add(video.getTitle())) {
                    String normalizedTitle = video.getTitle().toLowerCase().replaceAll("[^a-z0-9가-힣]", " ");
                    String normalizedKeyword = keyword.toLowerCase();
                    // 제목의 첫 단어가 키워드로 시작하는 경우만
                    String firstToken = Arrays.stream(normalizedTitle.split("\\s+"))
                            .findFirst()
                            .orElse("");
                    if (firstToken.startsWith(normalizedKeyword)) {
                        results.add(mapToResult(video));
                    }
                }
            }

            return results;

        } catch (IOException e) {
            log.error("Prefix 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // 제목에 포함된 모든 키워드에 대해 검색
    public List<VideoSearchResult> searchByAllKeywordsInTitle(List<String> keywords) {
        try {
            SearchResponse<SearchVideo> response = elasticsearchClient.search(s -> s
                            .index("tubelens_video")
                            .query(q -> q.bool(b -> {
                                for (String keyword : keywords) {
                                    b.must(m -> m.matchPhrase(mp -> mp.field("title.ko").query(keyword)));
                                }
                                return b;
                            }))
                            .sort(so -> so.field(f -> f.field("viewCount").order(SortOrder.Desc))),
                    SearchVideo.class);

            Set<String> seenTitles = new HashSet<>();
            List<VideoSearchResult> results = new ArrayList<>();

            for (var hit : response.hits().hits()) {
                SearchVideo v = hit.source();
                if (v != null && v.getTitle() != null && seenTitles.add(v.getTitle())) {
                    results.add(mapToResult(v));
                }
            }

            return results;

        } catch (IOException e) {
            log.error("모든 키워드 포함 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public static class KeywordGroup {
        public String title;
        public List<String> keywords;

        public KeywordGroup(String title, List<String> keywords) {
            this.title = title;
            this.keywords = keywords;
        }
    }

    // 제목에서 키워드를 추출하여 그룹화
    public List<KeywordGroup> extractKeywordGroupsFromTitles(List<VideoSearchResult> searchResults) {
        List<KeywordGroup> keywordGroups = new ArrayList<>();

        for (VideoSearchResult result : searchResults) {
            String title = result.getTitle();
            if (title == null || title.isBlank()) continue;

            String[] tokens = title.split("\\s+|[^가-힣a-zA-Z0-9]");
            List<String> keywords = Arrays.stream(tokens)
                    .filter(token -> token.length() >= 2)
                    .distinct()
                    .collect(Collectors.toList());

            if (!keywords.isEmpty()) {
                keywordGroups.add(new KeywordGroup(title, keywords));
            }
        }

        return keywordGroups;
    }

    // 키워드를 기반으로 추천어 그룹화
    public List<KeywordGroup> suggestKeywordGroupsFromSearch(String keyword, String fuzzinessLevel) {
        List<VideoSearchResult> results = searchByKeyword(keyword, fuzzinessLevel);
        return extractKeywordGroupsFromTitles(results);
    }

    // SearchVideo 객체를 VideoSearchResult로 변환
    private VideoSearchResult mapToResult(SearchVideo v) {
        VideoSearchResult result = new VideoSearchResult();
        result.setTitle(v.getTitle());
        result.setChannelTitle(v.getChannelTitle());
        result.setThumbnails(v.getThumbnails());
        result.setViewCount(v.getViewCount());
        return result;
    }
}