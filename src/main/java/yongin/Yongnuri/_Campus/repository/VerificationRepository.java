package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.Verification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {

    Optional<Verification> findByEmail(String email);
    // 10분 이상 지난 인증번호 삭제용
    List<Verification> findByCreatedAtBefore(LocalDateTime time);

    Optional<Verification> findTopByEmailOrderByCreatedAtDesc(String email);
}
