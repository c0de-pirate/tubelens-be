set -e
set -x

echo "Waiting for Elasticsearch to be ready..."

# Elasticsearch가 뜰 때까지 기다리기
until curl -s http://elasticsearch:9200 > /dev/null; do
  sleep 2
done

echo "Elasticsearch is ready."
echo "Installing template..."

# 템플릿 등록
curl -X PUT "http://elasticsearch:9200/_index_template/tubelens_video_template" \
     -H "Content-Type: application/json" \
     -d "@/tubelens_video_template.json"

echo "Template installed successfully."