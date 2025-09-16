// dto/useditem/UsedItemCreateRequestDto.java
package yongin.Yongnuri._Campus.dto.useditem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import yongin.Yongnuri._Campus.domain.UsedItem;
import java.time.LocalDateTime;
import java.util.List; // 리스트 임포트

@Getter
public class UsedItemCreateRequestDto {

    // "400: 데이터 미입력" 처리를 위한 Validation
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotBlank(message = "거래 장소는 필수입니다.")
    private String location;

    @NotBlank(message = "거래 방식은 필수입니다.")
    private String method;
    
    @NotBlank(message = "판매 상태는 필수입니다.")
    private String status; 

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    // (수정) Boolean isImages -> List<String> imageUrls
    // 이미지가 없으면 빈 리스트( [] )를 보내면 됩니다.
    private List<String> imageUrls; 

    // DTO를 UsedItem Entity로 변환하는 메서드
    public UsedItem toEntity(Long authorId) {
        return UsedItem.builder()
                .userId(authorId)
                .title(this.title)
                .content(this.content)
                .location(this.location)
                .method(this.method)
                .status(this.status)
                .price(this.price)
                .isImages(this.imageUrls != null && !this.imageUrls.isEmpty()) // URL 리스트가 비어있지 않으면 true
                .createdAt(LocalDateTime.now()) // Java에서 생성 시간 설정
                .build();
    }
}