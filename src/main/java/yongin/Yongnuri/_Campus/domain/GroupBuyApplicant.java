package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "group_buy_applicant")
public class GroupBuyApplicant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "post_id")
    private Long postId;
    @Column(name = "user_id")
    private Long userId;
    private String status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}