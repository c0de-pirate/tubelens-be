package codepirate.tubelensbe.video.repository.youtube;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class YouTubeRepositoryConfig {
    @Value("${youtube.api.key}")
    String apiKey;

    private final RestClient restClient = RestClient.builder()
            .defaultRequest(request -> request
                    .attribute("key", apiKey)
            )
            .build();
    private final RestClientAdapter adapter = RestClientAdapter.create(restClient);
    private final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();


    @Bean
    public YouTubeCommentsRepository commentsRepository() {
        return factory.createClient(YouTubeCommentsRepository.class);
    }
}
