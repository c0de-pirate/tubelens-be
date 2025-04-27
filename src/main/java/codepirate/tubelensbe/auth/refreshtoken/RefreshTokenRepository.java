package codepirate.tubelensbe.auth.refreshtoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    // 필요에 따라 사용자 ID로 토큰을 찾는 메서드 추가 가능
}