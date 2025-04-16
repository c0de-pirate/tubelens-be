package codepirate.tubelensbe.video.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class TrendingVideo {
    @Id
    private String id;

    private String title;

    private String thumbnail;

    @ElementCollection
    private List<String> tags;
}
