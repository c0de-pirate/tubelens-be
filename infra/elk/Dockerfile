ARG ELK_VERSION=8.12.2
FROM docker.elastic.co/elasticsearch/elasticsearch:${ELK_VERSION}

# MySQL JDBC 드라이버를 /usr/share/logstash/logstash-core/lib/jars/ 디렉토리에 복사
COPY mysql-connector-j-9.1.0.jar /usr/share/logstash/logstash-core/lib/jars/

# Logstash 실행
CMD ["logstash", "-f", "/usr/share/logstash/pipeline/logstash.conf"]

# nori 형태소 분석기 플러그인 설치
RUN elasticsearch-plugin install analysis-nori --batch