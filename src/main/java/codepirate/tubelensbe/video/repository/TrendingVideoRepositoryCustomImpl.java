package codepirate.tubelensbe.video.repository;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import lombok.RequiredArgsConstructor;
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

    public void batchInsertIgnore(List<TrendingVideo> videoList) {
        String sql = """
            INSERT IGNORE INTO trending_video (
                id, title, thumbnails, embed_html,
                description, channel_title, view_count, like_count, comment_count, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TrendingVideo v = videoList.get(i);
                ps.setString(1, v.getId());
                ps.setString(2, v.getTitle());
                ps.setString(3, v.getThumbnails());
                ps.setString(4, v.getEmbedHtml());
                ps.setString(5, v.getDescription());
                ps.setString(6, v.getChannelTitle());
                ps.setObject(7, v.getViewCount());
                ps.setObject(8, v.getLikeCount());
                ps.setObject(9, v.getCommentCount());
                ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
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