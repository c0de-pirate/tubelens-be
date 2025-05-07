from pydantic import BaseModel
from typing import List

class Comment(BaseModel):
    commentId: str
    content: str

class CommentsSentiment(BaseModel):
    commentId: str
    content: str
    sentimentType: str
    score: float

class CommentSentimentResponse(BaseModel):
    comments: List[CommentsSentiment]