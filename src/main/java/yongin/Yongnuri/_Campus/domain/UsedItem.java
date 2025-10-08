package yongin.Yongnuri._Campus.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Enum.UsedItemStatus;

@Entity
@Getter
@Setter
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@Table(name="used_item")
public class UsedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id") 
    private Long userId; 

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "is_images") 
    private Boolean isImages;

    @Column(name = "method") 
    private String method;



    @Column(name = "location") 
    private String location; 

    @Column(name = "price")
    private Integer price;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private UsedItemStatus status;
/**
     @Enumerated(EnumType.STRING)
     private UsedItemStatus status;
    public enum UsedItemStatus {
        SELLING,
        RESERVED,
        SOLD,
        DELETED
    }
*/
}