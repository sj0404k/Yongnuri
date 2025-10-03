package yongin.Yongnuri._Campus.dto.useditem;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import yongin.Yongnuri._Campus.domain.GroupBuy;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.domain.Enum;
@Getter
public class UsedItemCreateRequestDto {

    
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

    
    private List<String> imageUrls;


    public UsedItem toEntity(Long authorId) {
        return UsedItem.builder()
                .userId(authorId)
                .title(this.title)
                .content(this.content)
                .location(this.location)
                .method(this.method)
                .status(Enum.UsedItemStatus.valueOf(this.status.toUpperCase()))
                .price(this.price)
                .isImages(this.imageUrls != null && !this.imageUrls.isEmpty()) // 내부에서 계산
                .createdAt(LocalDateTime.now())
                .build();
    }
}