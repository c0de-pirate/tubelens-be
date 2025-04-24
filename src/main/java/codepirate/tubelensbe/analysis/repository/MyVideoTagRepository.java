package codepirate.tubelensbe.analysis.repository;

import codepirate.tubelensbe.analysis.domain.MyVideoTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MyVideoTagRepository extends JpaRepository<MyVideoTag, Long> {
    boolean existsByLastUpdated(LocalDate date);
    Optional<MyVideoTag> findByTag(String tag);
}