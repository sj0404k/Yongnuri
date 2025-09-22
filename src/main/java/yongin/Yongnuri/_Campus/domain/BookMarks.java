package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookMarks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private Long userId;

    @Column
    private PostType postType;

    @Column
    private Long postId;

    @Column
    private LocalDateTime createdAt;

    public enum PostType {
        전체,
        중고,
        분실,
        공동구매
    }
}
