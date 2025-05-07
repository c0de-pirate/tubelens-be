from transformers import pipeline
from googletrans import Translator
from model.sentiment import Comment, CommentsSentiment

# 파이프라인 및 번역기 초기화
classifier = pipeline("sentiment-analysis", model="distilbert-base-uncased-finetuned-sst-2-english")
translator = Translator()


def analyze_comments(comments: list[Comment]) -> dict:
    results = []
    for comment in comments:
        translated = translator.translate(comment.content, src='ko', dest='en').text
        analysis = classifier(translated)[0]
        results.append(CommentsSentiment(
            commentId=comment.commentId,
            content=comment.content,
            sentimentType=analysis["label"].upper(),
            score=round(analysis["score"], 4)
        ))
    return {"comments": results}