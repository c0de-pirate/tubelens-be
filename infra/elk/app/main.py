import re
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from krwordrank.word import KRWordRank
from sentence_transformers import SentenceTransformer

app = FastAPI()

# 시작하면 임베딩 로딩 (1`번만 로딩)
model = SentenceTransformer("snunlp/KR-SBERT-V40K-klueNLI-augSTS")

@app.post("/refine-title")
async def refine_title(request: Request):
    data = await request.json()
    title = data.get("title", "")

    try:
        # 1. 특문 제거
        cleaned_title = re.sub(r"[^가-힣a-zA-Z0-9\s]", "", title)

        # 2. 키워드 추출
        texts = [cleaned_title]
        wordrank_extractor = KRWordRank(min_count=1, max_length=10, verbose=False)
        keywords, _, _ = wordrank_extractor.extract(texts, beta=0.85, max_iter=10)

        sorted_keywords = sorted(keywords.items(), key=lambda x: x[1], reverse=True)
        top_keywords = [word for word, score in sorted_keywords[:3]]
        refined_title = ",".join(top_keywords)

        # 3. 임베딩 (키워드 3개를 하나로 합쳐서 처리)
        sentence = " ".join(top_keywords)
        print(f"인코딩된 문장: {sentence}")  # 디버깅용 로그
        embedding = model.encode(sentence).tolist()
        print(f"임베딩 생성된거: {embedding[:5]}...")  # 디버깅용 일부 출력

        return {
            "refined_title": refined_title,
            "embedding": embedding
        }

    except Exception as e:
        print(f"KRWordRank or embedding error: {e}")
        return JSONResponse(
            status_code=500,
            content={
                "error": "Processing error",
                "details": str(e)
            }
        )
