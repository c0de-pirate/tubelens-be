package codepirate.tubelensbe.video.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class JsonBodyHandler<T> implements HttpResponse.BodyHandler<T> {
    private final Class<T> targetType;
    private final ObjectMapper objectMapper;

    public JsonBodyHandler(Class<T> targetType) {
        this.targetType = targetType;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        return HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
                body -> {
                    try {
                        return objectMapper.readValue(body, targetType);
                    } catch (Exception e) {
                        throw new RuntimeException("JSON 역직렬화 실패", e);
                    }
                }
        );
    }
}
