package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "pass_word")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "major")
    private String major;

    @Column(name = "status")
    private String status;

    @Column(name = "creat_at")
    private Date creatAt;

}
