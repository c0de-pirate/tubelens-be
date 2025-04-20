from fastapi import FastAPI, Request
import openai
import os

openai.api_key = os.getenv("CHATGPT_API_KEY")

app = FastAPI()

@app.post("/refine-title")
async def refine_title(request: Request):
    data = await request.json()
    title = data.get("title", "")
    response = openai.ChatCompletion.create(
        model="gpt-4o-mini",
        messages=[{
            "role": "user",
            "content": f"Analyze the following Korean YouTube title and perform Preprocessing and Query Expansion so that it can be used for search, while maximally preserving the original meaning, within a length of 5 to 10 characters: {title}"
        }],
        max_tokens=50,
        temperature=0.6
    )
    refined_title = response.choices[0].message.content.strip()
    return {"refined_title": refined_title}