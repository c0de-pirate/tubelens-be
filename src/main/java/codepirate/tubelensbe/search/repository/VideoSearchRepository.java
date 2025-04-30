package codepirate.tubelensbe.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import codepirate.tubelensbe.search.dto.VideoSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import codepirate.tubelensbe.search.domain.SearchVideo;

import java.io.IOException;
import java.util.*;


@Slf4j
@Repository
@RequiredArgsConstructor
public class VideoSearchRepository {

    private final ElasticsearchClient elasticsearchClient;

    public List<VideoSearchResult> searchByKeyword(String keyword, String fuzzinessLevel) {
        try {
            SearchResponse<SearchVideo> exactMatchResponse = elasticsearchClient.search(s -> s.index("tubelens_videos").query(q -> q.bool(b -> b.should(sh -> sh.matchPhrase(mp -> mp.field("title.ko").query(keyword))).should(sh -> sh.matchPhrase(mp -> mp.field("title.en").query(keyword))).minimumShouldMatch("1"))).sort(so -> so.field(f -> f.field("view_count").order(SortOrder.Desc))), SearchVideo.class);

            SearchResponse<SearchVideo> fuzzyMatchResponse = elasticsearchClient.search(s -> s.index("tubelens_videos").query(q -> q.bool(b -> b.should(sh -> sh.match(m -> m.field("title.ko").query(keyword).fuzziness(fuzzinessLevel))).should(sh -> sh.match(m -> m.field("title.en").query(keyword).fuzziness(fuzzinessLevel))).minimumShouldMatch("1"))).sort(so -> so.field(f -> f.field("view_count").order(SortOrder.Desc))), SearchVideo.class);

            Set<String> seenTitles = new HashSet<>();
            List<VideoSearchResult> results = new ArrayList<>();

            addUniqueResults(exactMatchResponse, seenTitles, results);
            addUniqueResults(fuzzyMatchResponse, seenTitles, results);

            return results;

        } catch (IOException e) {
            log.error("Elasticsearch 검색 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<VideoSearchResult> searchByPrefix(String keyword) {
        try {
            SearchResponse<SearchVideo> response = elasticsearchClient.search(s -> s.index("tubelens_videos").query(q -> q.bool(b -> b.should(sh -> sh.prefix(p -> p.field("title.ko").value(keyword))).boost(2.0f).should(sh -> sh.prefix(p -> p.field("title.en").value(keyword))).boost(2.0f).minimumShouldMatch("1"))).sort(so -> so.field(f -> f.field("view_count").order(SortOrder.Desc))), SearchVideo.class);

            Set<String> seenTitles = new HashSet<>();
            List<VideoSearchResult> results = new ArrayList<>();

            for (Hit<SearchVideo> hit : response.hits().hits()) {
                SearchVideo video = hit.source();
                if (video != null && video.getTitle() != null && seenTitles.add(video.getTitle())) {
                    String normalizedTitle = video.getTitle().toLowerCase().replaceAll("[^a-z0-9가-힣]", " ");
                    String normalizedKeyword = keyword.toLowerCase();
                    String firstToken = Arrays.stream(normalizedTitle.split("\\s+")).findFirst().orElse("");
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

    public List<VideoSearchResult> searchByContains(String keyword) {
        try {
            SearchResponse<SearchVideo> response = elasticsearchClient.search(s -> s.index("tubelens_videos").query(q -> q.match(m -> m.field("title.ko").query(keyword).operator(Operator.And))).sort(so -> so.field(f -> f.field("view_count").order(SortOrder.Desc))).size(50), SearchVideo.class);

            Set<String> seenTitles = new HashSet<>();
            List<VideoSearchResult> results = new ArrayList<>();

            addUniqueResults(response, seenTitles, results);

            return results;

        } catch (IOException e) {
            log.error("제목에 키워드 포함 검색 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<VideoSearchResult> searchByInputOrKeywords(String input, List<String> keywords, String fuzzinessLevel) {
        try {
            SearchResponse<SearchVideo> response = elasticsearchClient.search(s -> s.index("tubelens_videos").query(q -> q.bool(b -> {
                for (String keyword : keywords) {
                    b.should(sh -> sh.match(m -> m.field("title.ko").query(keyword).fuzziness(fuzzinessLevel)));
                }
                b.should(sh -> sh.match(m -> m.field("title.ko").query(input).fuzziness(fuzzinessLevel)));
                b.minimumShouldMatch("1");
                return b;
            })).sort(so -> so.field(f -> f.field("_score").order(SortOrder.Desc))), SearchVideo.class);

            Set<String> seenTitles = new HashSet<>();
            List<VideoSearchResult> results = new ArrayList<>();

            addUniqueResults(response, seenTitles, results);

            return results;

        } catch (IOException e) {
            log.error("Input OR keywords fuzzy 검색 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }


    private void addUniqueResults(SearchResponse<SearchVideo> response, Set<String> seenTitles, List<VideoSearchResult> results) {
        for (Hit<SearchVideo> hit : response.hits().hits()) {
            SearchVideo video = hit.source();
            if (video != null && video.getTitle() != null && seenTitles.add(video.getTitle())) {
                results.add(mapToResult(video));
            }
        }
    }

    private VideoSearchResult mapToResult(SearchVideo v) {
        VideoSearchResult result = new VideoSearchResult();
        result.setId(v.getId());
        result.setTitle(v.getTitle());
        result.setChannelTitle(v.getChannelTitle());
        result.setThumbnails(v.getThumbnails());
        result.setViewCount(v.getViewCount());
        result.setEmbedHtml(v.getEmbedHtml());
        return result;
    }

}