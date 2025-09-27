package yongin.Yongnuri._Campus.domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 게시자
    @Enumerated(EnumType.STRING)
    private ItemPurpose purpose; // LOST,FOUND
    @Enumerated(EnumType.STRING)
    private ItemStatus status; // REPORTED, RETURNED,DELETED
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String location;
    @Column(name = "is_images")
    private Boolean isImages;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    public enum ItemPurpose {
        LOST, FOUND
    }
    public enum ItemStatus {
        REPORTED, RETURNED, DELETED
    }
}