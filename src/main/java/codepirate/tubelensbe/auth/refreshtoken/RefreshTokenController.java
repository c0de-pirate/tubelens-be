package codepirate.tubelensbe.auth.refreshtoken;

import codepirate.tubelensbe.auth.exception.TokenRefreshException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/refresh")
public class RefreshTokenController {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is required!");
        }

        try {
            String accessToken = refreshTokenService.refreshAccessToken(refreshToken);
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            return ResponseEntity.ok(response);
        } catch (TokenRefreshException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }
}