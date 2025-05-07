FROM python:3.10-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
RUN apt-get update && apt-get install -y gcc git && rm -rf /var/lib/apt/lists/*

# 소스 코드 복사
COPY main.py .
COPY model/ ./model/
COPY service/ ./service/
COPY api/ ./api/

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]