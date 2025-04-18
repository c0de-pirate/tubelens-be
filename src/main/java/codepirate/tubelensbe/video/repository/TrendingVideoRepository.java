package codepirate.tubelensbe.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import codepirate.tubelensbe.video.domain.TrendingVideo;

import java.util.List;

@Repository
public interface TrendingVideoRepository extends JpaRepository<TrendingVideo, String> {
    @Override
    <S extends TrendingVideo> List<S> saveAll(Iterable<S> entities);
}
