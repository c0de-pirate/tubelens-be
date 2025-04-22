package codepirate.tubelensbe.video.repository.python;

import codepirate.tubelensbe.video.dto.Comment;
import codepirate.tubelensbe.video.dto.SentimentVectorResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@Component
@HttpExchange("http://localhost:3000")
public interface PythonRepository {

    @PostExchange("/encode")
    SentimentVectorResponse sentimentVector(
            @RequestBody List<Comment> comments
    );
}
