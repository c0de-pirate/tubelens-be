package codepirate.tubelensbe.auth.oauth2.handler;

import codepirate.tubelensbe.auth.oauth2.service.OAuth2UserProcessingService;
import codepirate.tubelensbe.auth.refreshtoken.TokenExtractor;
import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.auth.oauth2.service.CustomOAuth2UserService;
import codepirate.tubelensbe.auth.oauth2.user.GoogleOAuth2UserInfo;
import codepirate.tubelensbe.auth.jwt.JwtTokenProvider;
import codepirate.tubelensbe.auth.refreshtoken.RefreshToken;
import codepirate.tubelensbe.auth.refreshtoken.RefreshTokenService;
import codepirate.tubelensbe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2UserProcessingService processingService;
    private final RefreshTokenService refreshTokenService;
    private final TokenExtractor tokenExtractor;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpirySeconds;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${server.react.port}")
    private String port;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = processingService.processOAuth2User(oAuth2User);

        String accessToken = jwtTokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(user.getName(), null, oAuth2User.getAuthorities()));

        RefreshToken refreshToken = getOrCreateRefreshToken(user);

        String frontendUrl = String.format("http://localhost:%s/", port);
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken.getToken())
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }

    private RefreshToken getOrCreateRefreshToken(User user) {
        Optional<RefreshToken> existingTokenOpt = refreshTokenService.findByUserId(user.getId());

        if (existingTokenOpt.isPresent()) {
            RefreshToken existingToken = existingTokenOpt.get();
            if (!refreshTokenService.isTokenExpired(existingToken)) {
                return existingToken;
            }
        }

        return refreshTokenService.createRefreshToken(user);
    }
}