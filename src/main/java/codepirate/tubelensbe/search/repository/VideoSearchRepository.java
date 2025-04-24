package codepirate.tubelensbe.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
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
                            .sort(so -> so.field(f -> f.field("view_count").order(SortOrder.Desc))),
                    SearchVideo.class);

            SearchResponse<SearchVideo> fuzzyMatchResponse = elasticsearchClient.search(s -> s
                            .index("tubelens_video")
                            .query(q -> q.bool(b -> b
                                    .should(sh -> sh.match(m -> m.field("title.ko").query(keyword).fuzziness(fuzzinessLevel)))
                                    .should(sh -> sh.match(m -> m.field("title.en").query(keyword).fuzziness(fuzzinessLevel)))
                                    .minimumShouldMatch("1")
                            ))
                            .sort(so -> so.field(f -> f.field("view_count").order(SortOrder.Desc))),
                    SearchVideo.class);

            Set<String> seenTitles = new HashSet<>();
            List<VideoSearchResult> results = new ArrayList<>();

            Consumer<SearchVideo> addIfNotDuplicate = v -> {
                if (v != null && v.getTitle() != null && seenTitles.add(v.getTitle())) {
                    VideoSearchResult result = new VideoSearchResult();
                    result.setTitle(v.getTitle());
                    result.setChannelTitle(v.getChannelTitle());
                    result.setThumbnails(v.getThumbnails());
                    result.setViewCount(v.getViewCount());
                    results.add(result);
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
                            .sort(so -> so.field(f -> f.field("view_count").order(SortOrder.Desc))),
                    SearchVideo.class);

            Set<String> seenTitles = new HashSet<>();
            List<VideoSearchResult> results = new ArrayList<>();

            for (var hit : response.hits().hits()) {
                SearchVideo v = hit.source();
                if (v != null && v.getTitle() != null && seenTitles.add(v.getTitle())) {
                    VideoSearchResult result = new VideoSearchResult();
                    result.setTitle(v.getTitle());
                    result.setChannelTitle(v.getChannelTitle());
                    result.setThumbnails(v.getThumbnails());
                    result.setViewCount(v.getViewCount());
                    results.add(result);
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

    // title별 키워드 그룹 객체 반환
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

    public List<KeywordGroup> suggestKeywordGroupsFromSearch(String keyword, String fuzzinessLevel) {
        List<VideoSearchResult> results = searchByKeyword(keyword, fuzzinessLevel);
        return extractKeywordGroupsFromTitles(results);
    }
}