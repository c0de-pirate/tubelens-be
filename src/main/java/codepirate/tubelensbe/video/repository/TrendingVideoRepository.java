package codepirate.tubelensbe.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import codepirate.tubelensbe.video.domain.TrendingVideo;

import java.util.List;

@Repository
public interface TrendingVideoRepository extends JpaRepository<TrendingVideo, String> {
//    @Override
//    <S extends TrendingVideo> List<S> saveAll(Iterable<S> entities);

    @Modifying
    @Query(
            value = """
        INSERT IGNORE INTO trending_video (
            id, title, thumbnails, embedHtml, publishedAt, description,
            channelTitle, viewCount, likeCount, commentCount, tags
        )
        VALUES (
            :id, :title, :thumbnails, :embedHtml, :publishedAt, :description,
            :channelTitle, :viewCount, :likeCount, :commentCount, :tags
        )
    """,
            nativeQuery = true
    )
    void saveOnly(    @Param("id") String id,
                      @Param("title") String title,
                      @Param("thumbnails") String thumbnails,
                      @Param("embedHtml") String embedHtml,
                      @Param("publishedAt") String publishedAt,
                      @Param("description") String description,
                      @Param("channelTitle") String channelTitle,
                      @Param("viewCount") Long viewCount,
                      @Param("likeCount") Long likeCount,
                      @Param("commentCount") Long commentCount,
                      @Param("tags") List<String> tags
    );
}
