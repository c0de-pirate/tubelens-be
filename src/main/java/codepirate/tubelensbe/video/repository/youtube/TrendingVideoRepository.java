package codepirate.tubelensbe.video.repository.youtube;

import codepirate.tubelensbe.video.domain.TrendingVideo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrendingVideoRepository extends JpaRepository<TrendingVideo, String> {

    // 날짜 제한 없는 조회 메서드
    List<TrendingVideo> findByOrderByViewCountDesc(Pageable pageable);
    List<TrendingVideo> findByOrderByLikeCountDesc(Pageable pageable);

    // 날짜 범위가 있는 조회 메서드
    List<TrendingVideo> findByUpdated_atBetweenOrderByViewCountDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    List<TrendingVideo> findByUpdated_atBetweenOrderByLikeCountDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}