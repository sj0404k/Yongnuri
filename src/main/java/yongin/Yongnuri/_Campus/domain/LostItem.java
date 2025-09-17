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

@Entity
@Getter
@Setter // 간단한 예시를 위해 Setter 사용
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
    private ItemPurpose purpose; // 게시 목적 (LOST, FOUND)

    @Enumerated(EnumType.STRING)
    private ItemStatus status; // 처리 상태 (REPORTED, RETURNED)

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String location;

    // created_at, updated_at 등 타임스탬프 필드 (BaseEntity 등으로 분리하는 것이 좋음)

    public enum ItemPurpose {
        LOST, FOUND
    }

    public enum ItemStatus {
        REPORTED, RETURNED
    }
}