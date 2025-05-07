from fastapi import APIRouter
from model.sentiment import Comment, CommentSentimentResponse
from service.sentiment_service import analyze_comments
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

router = APIRouter()

@router.post("/encode", response_model=CommentSentimentResponse)
async def encode(comments: list[Comment]):
    logger.info(f"감정분석 요청 받음: {len(comments)}개 댓글")
    try:
        result = analyze_comments(comments)
        logger.info(f"감정분석 완료: {len(result['comments'])}개 결과")
        return result
    except Exception as e:
        logger.error(f"감정분석 중 오류 발생: {e}")
        raise