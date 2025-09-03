package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    @Id
    @Column(unique = true,name = "user_id")
    private String userId;

    @Column(name = "assess_token")
    private String assessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "assess_date")
    private Date assessDate;

}
