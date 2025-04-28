package codepirate.tubelensbe.analysis.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "my_video_tag", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"channelId", "tag"})})
public class MyVideoTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String channelId;

    @Column(nullable = false)
    private String tag;

    @Column(nullable = false)
    private Integer count;

    @Column(nullable = false)
    private LocalDate lastUpdated;

    public MyVideoTag(String channelId, String tag, Integer count, LocalDate lastUpdated) {
        this.channelId = channelId;
        this.tag = tag;
        this.count = count;
        this.lastUpdated = lastUpdated;
    }
}
