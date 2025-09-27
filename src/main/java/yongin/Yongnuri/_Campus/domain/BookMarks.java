package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookMarks {
   /**
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
        */
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_type", nullable = false)
    private String postType; // "USED_ITEM", "LOST_ITEM"

    @Column(name = "post_id", nullable = false)
    private Long postId;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
