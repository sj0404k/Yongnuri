package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    private String email;  // 사용자 식별용 (PK)
    private String token;  // Refresh Token 값

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
