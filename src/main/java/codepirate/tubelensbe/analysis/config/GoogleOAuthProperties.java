package codepirate.tubelensbe.analysis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google.oauth")
@Getter
@Setter
public class GoogleOAuthProperties {
    private String accessToken;
    private String refreshToken;
    private String clientId;
    private String clientSecret;
}
