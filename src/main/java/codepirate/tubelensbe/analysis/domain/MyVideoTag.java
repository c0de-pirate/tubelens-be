package codepirate.tubelensbe.analysis.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "my_video_tag")
public class MyVideoTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tag;

    @Column(nullable = false)
    private int count;

    @Column(nullable = false)
    private LocalDate lastUpdated;
}
