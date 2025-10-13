package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yongin.Yongnuri._Campus.domain.Enum.UserRole;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "student_id",unique = true)
    private int studentId;

    @Column(name = "pass_word")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "major")
    private String major;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Enum.authStatus status;

    @Column(name = "creat_at")
    private LocalDateTime creatAt;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(columnDefinition = "TEXT",nullable = true)
    private String text="";
/**
    @Column(name = "role")
    private Role role;
    public enum Role {
        USER,
        ADMIN
    }
*/
}
