package codepirate.tubelensbe.analysis.repository;

import codepirate.tubelensbe.analysis.domain.MyVideoTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MyVideoTagRepository extends JpaRepository<MyVideoTag, Long> {
    List<MyVideoTag> findByChannelIdAndLastUpdated(String channelId, LocalDate lastUpdated);
    List<MyVideoTag> findByChannelId(String channelId);
}