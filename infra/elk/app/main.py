import os
os.environ["NLTK_DATA"] = "/usr/share/nltk_data"  # 이 줄 추가

from fastapi import FastAPI, Request
from rake_nltk import Rake

app = FastAPI()

@app.post("/refine-title")
async def refine_title(request: Request):
    data = await request.json()
    title = data.get("title", "")
    try:
        r = Rake(language='kor')
        r.extract_keywords_from_text(title)
        ranked_phrases = r.get_ranked_phrases()
        top_keywords = ranked_phrases[:3]
        refined_title = ",".join(top_keywords)
        return {"refined_title": refined_title}
    except Exception as e:
        print(f"Rake-NLTK error: {e}")
        return {"error": "Rake-NLTK error", "details": str(e)}, 500