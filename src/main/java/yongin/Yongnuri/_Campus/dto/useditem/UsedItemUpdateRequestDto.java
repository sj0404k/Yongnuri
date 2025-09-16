// dto/useditem/UsedItemUpdateRequestDto.java
package yongin.Yongnuri._Campus.dto.useditem;

import java.util.List;

import lombok.Getter;

// 생성(Create) DTO와 달리, Validation(@NotBlank 등)이 없습니다.
// null이 아닌 필드만 서비스 로직에서 업데이트할 것입니다.
@Getter
public class UsedItemUpdateRequestDto {

    private String title;
    private String content;
    private String location;
    private String method;
    private String status; 
    private Integer price;
    private List<String> imageUrls; // 이미지 목록 (전체 교체용)

}