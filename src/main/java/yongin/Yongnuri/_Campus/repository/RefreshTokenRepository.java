package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yongin.Yongnuri._Campus.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
}
