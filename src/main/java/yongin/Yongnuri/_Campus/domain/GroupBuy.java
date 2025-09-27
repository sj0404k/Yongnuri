package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_buy")
public class GroupBuy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private Boolean isImages;
    @Enumerated(EnumType.STRING)
    private GroupBuyStatus status;
    private String link;
    @Column(name = "`limit`")
    private Integer limit;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Builder.Default
    private Integer currentCount = 0;
    public enum GroupBuyStatus {
        RECRUITING,
        COMPLETED,
        DELETED
    }
}