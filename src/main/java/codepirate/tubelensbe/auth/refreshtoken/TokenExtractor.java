package codepirate.tubelensbe.auth.refreshtoken;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TokenExtractor {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public TokenExtractor(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public String extractAccessToken(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauth2Token.getAuthorizedClientRegistrationId(),
                    oauth2Token.getName()
            );

            if (client != null && client.getAccessToken() != null) {
                return client.getAccessToken().getTokenValue();
            }
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            // JWT 기반 인증인 경우
            if (authentication.getCredentials() != null) {
                return authentication.getCredentials().toString();
            }
        }

        return null;
    }

    public String extractRefreshToken(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauth2Token.getAuthorizedClientRegistrationId(),
                    oauth2Token.getName()
            );

            if (client != null && client.getRefreshToken() != null) {
                return client.getRefreshToken().getTokenValue();
            }
        }

        return null;
    }
}
