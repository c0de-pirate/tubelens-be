package codepirate.tubelensbe.auth.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private long accessTokenExpirationMs = 30 * 60 * 1000; // Access Token 유효 시간 (30분)
    private long refreshTokenExpirationMs = 7 * 24 * 60 * 60 * 1000; // Refresh Token 유효 시간 (7일)
    private String tokenHeader = "Authorization";
    private String tokenPrefix = "Bearer ";

}