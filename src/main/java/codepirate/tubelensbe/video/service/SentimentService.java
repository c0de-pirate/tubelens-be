//package codepirate.tubelensbe.video.service;
//
//import codepirate.tubelensbe.video.domain.CommentSentiment;
//import codepirate.tubelensbe.video.domain.SentimentType;
//import codepirate.tubelensbe.video.dto.Comment;
//import codepirate.tubelensbe.video.dto.SentimentPercent;
//import codepirate.tubelensbe.video.dto.SentimentVectorResponse;
//import codepirate.tubelensbe.video.dto.YouTubeCommentResponse;
//import codepirate.tubelensbe.video.repository.python.PythonRepository;
//import codepirate.tubelensbe.video.repository.youtube.YouTubeCommentsRepository;
//import codepirate.tubelensbe.video.repository.elasticsearch.CommentSentimentEsRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class SentimentService {
//
//    @Value("${youtube.api.key}")
//    private String YOUTUBE_API_KEY;
//
//    private final YouTubeCommentsRepository youTubeCommentsRepository;
//    private final PythonRepository pythonRepository;
//    private final CommentSentimentEsRepository commentSentimentEsRepository;
//
//    /**
//     * 댓글 감정 분석
//     * <p> 유튜브 댓글을 통해 감정을 분석하고, 각 감정을 %로 반환합니다.
//     *
//     * @param videoId
//     * @return Map
//     */
//    public SentimentPercent getYoutubeComments(String videoId) {
//        YouTubeCommentResponse youTubeCommentResponse = youTubeCommentsRepository.getComments(
//                "snippet",
//                videoId,
//                YOUTUBE_API_KEY
//        );
//
//        List<Comment> comments = new ArrayList<>();
//        youTubeCommentResponse.getItems().forEach(commentJson -> {
//            Comment comment = Comment.builder()
//                    .commentId(commentJson.getId())
//                    .content(commentJson.getSnippet().getTopLevelComment().getSnippet().getTextDisplay())
//                    .build();
//            comments.add(comment);
//        });
//        comments.removeFirst();
//
//        SentimentVectorResponse vectors = pythonRepository.sentimentVector(comments);
//
//        vectors.getComments().forEach(commentJson -> {
//            commentSentimentEsRepository.save(
//                    CommentSentiment.builder()
//                            .id(commentJson.getCommentId())
//                            .videoId(videoId)
//                            .content(commentJson.getContent())
//                            .sentimentType(commentJson.getSentimentType())
//                            .score(commentJson.getScore())
//                            .build()
//            );
//        });
//        int total = comments.size();
//        double positive = vectors.getComments().stream().filter(c -> c.getSentimentType() == SentimentType.POSITIVE).count();
//        double neutral = vectors.getComments().stream().filter(c -> c.getSentimentType() == SentimentType.NEUTRAL).count();
//        double negative = vectors.getComments().stream().filter(c -> c.getSentimentType() == SentimentType.NEGATIVE).count();
//
//        return SentimentPercent.builder()
//                .positive(positive / total * 100)
//                .neutral(neutral / total * 100)
//                .negative(negative / total * 100)
//                .build();
//    }
//}
