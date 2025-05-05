package codepirate.tubelensbe.auth.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // JSON 형태로 에러 응답 커스터마이징
        String jsonResponse = String.format(
                "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\"}",
                new java.util.Date(),
                HttpServletResponse.SC_UNAUTHORIZED,
                authException.getMessage() != null ? authException.getMessage() : "인증이 필요한 리소스입니다.",
                request.getRequestURI()
        );

        response.getWriter().write(jsonResponse);
    }
}