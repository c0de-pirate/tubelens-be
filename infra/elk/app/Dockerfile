FROM python:3.10-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# NLTK 데이터 다운로드 경로 명시적 지정
RUN python -m nltk.downloader -d /usr/share/nltk_data stopwords

# NLTK_DATA 환경 변수 설정
ENV NLTK_DATA=/usr/share/nltk_data

COPY main.py .

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]