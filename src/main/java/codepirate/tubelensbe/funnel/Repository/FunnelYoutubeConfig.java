package codepirate.tubelensbe.funnel.Repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class FunnelYoutubeConfig {

    private final RestClient restClient = RestClient.builder().build();
    private final RestClientAdapter adapter = RestClientAdapter.create(restClient);
    private final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

    @Bean
    public FunnelYoutubeRepository trafficSourceRepository() {
        return factory.createClient(FunnelYoutubeRepository.class);
    }
}
