import re
from fastapi import FastAPI, Request
from krwordrank.word import KRWordRank

app = FastAPI()

@app.post("/refine-title")
async def refine_title(request: Request):
    data = await request.json()
    title = data.get("title", "")

    try:
        # 특수문자 제거 (한글, 숫자, 영어만 남기기)
        cleaned_title = re.sub(r"[^가-힣a-zA-Z0-9\s]", "", title)

        texts = [cleaned_title]
        wordrank_extractor = KRWordRank(min_count=1, max_length=10, verbose=False)
        keywords, _, _ = wordrank_extractor.extract(texts, beta=0.85, max_iter=10)

        sorted_keywords = sorted(keywords.items(), key=lambda x: x[1], reverse=True)
        top_keywords = [word for word, score in sorted_keywords[:3]]

        refined_title = ",".join(top_keywords)
        return {"refined_title": refined_title}

    except Exception as e:
        print(f"KRWordRank error: {e}")
        return {"error": "KRWordRank error", "details": str(e)}, 500