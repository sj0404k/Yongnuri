package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByNickName(String nickName);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
