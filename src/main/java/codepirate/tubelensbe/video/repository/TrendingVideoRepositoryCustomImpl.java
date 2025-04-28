package codepirate.tubelensbe.video.repository;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import codepirate.tubelensbe.video.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TrendingVideoRepositoryCustomImpl implements TrendingVideoRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    public void batchInsertIgnore(List<TrendingVideo> videoList) {
        String sql = """
            INSERT IGNORE INTO trending_video (
                id, title, thumbnails, embed_html, published_at,
                description, channel_title, view_count, like_count, comment_count, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TrendingVideo v = videoList.get(i);

                LocalDateTime publishedAt = v.getPublishedAt().toLocalDateTime();

                Long viewCount = (v.getViewCount() != null) ? v.getViewCount() : 0L;
                Long likeCount = (v.getLikeCount() != null) ? v.getLikeCount() : 0L;
                Long commentCount = (v.getCommentCount() != null) ? v.getCommentCount() : 0L;

                ps.setString(1, v.getId());
                ps.setString(2, v.getTitle());
                ps.setString(3, v.getThumbnails());
                ps.setString(4, v.getEmbedHtml());
                ps.setTimestamp(5, Timestamp.valueOf(publishedAt));
                ps.setString(6, v.getDescription());
                ps.setString(7, v.getChannelTitle());
                ps.setLong(8, viewCount);
                ps.setLong(9, likeCount);
                ps.setLong(10, commentCount);
                ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            }

            public int getBatchSize() {
                return videoList.size();
            }
        });

        String tagSql = "INSERT IGNORE INTO trending_video_tags (trending_video_id, tags) VALUES (?, ?)";

        List<String[]> tagParams = new ArrayList<>();
        for (TrendingVideo v : videoList) {
            if (v.getTags() != null) {
                for (String tag : v.getTags()) {
                    tagParams.add(new String[]{v.getId(), tag});
                }
            }
        }

        jdbcTemplate.batchUpdate(tagSql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, tagParams.get(i)[0]);
                ps.setString(2, tagParams.get(i)[1]);
            }

            public int getBatchSize() {
                return tagParams.size();
            }
        });
    }
}