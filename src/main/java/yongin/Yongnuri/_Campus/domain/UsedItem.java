package yongin.Yongnuri._Campus.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity; 
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; 
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@Table
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

    @Column(name = "status")
    private String status;

    @Column(name = "location") 
    private String location; 

    @Column(name = "price")
    private Integer price;

    @Column(name = "created_at") 
    private LocalDateTime createdAt;
    

}