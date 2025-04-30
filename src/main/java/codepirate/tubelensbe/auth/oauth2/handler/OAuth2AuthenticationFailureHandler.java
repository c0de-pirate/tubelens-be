package codepirate.tubelensbe.auth.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String redirectUrl;
        redirectUrl = UriComponentsBuilder.fromUriString("/social/login/error") // 프론트엔드 URI
                .queryParam("error", "oauth2")
                .queryParam("message", exception.getMessage())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);

        // 로깅
        exception.printStackTrace();
    }
}