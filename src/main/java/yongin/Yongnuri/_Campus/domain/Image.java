// domain/Image.java
package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "image") 
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "type")
    private String type; // 예: "USED_ITEM", "LOST_ITEM", "CHAT"

    @Column(name = "type_id")
    private Long typeId; // 예: 게시글의 ID 또는 채팅방 ID

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sequence")
    private Integer sequence; // 이미지 순서 (1, 2, 3...)
}