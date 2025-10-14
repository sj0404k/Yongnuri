package yongin.Yongnuri._Campus.dto.useditem;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 게시글의 판매 상태(SELLING / RESERVED / SOLD / DELETED)를 변경하기 위한 요청 DTO
 * - RESERVED / SOLD 상태로 변경할 때는 buyerId(구매자 ID) 필드를 함께 사용
 * - 프론트에서는 상태 셀렉터에서 선택한 구매자의 userId를 함께 전송하도록 설계
 */
@Getter
@Setter
public class UpdateStatusRequestDto {

    @NotBlank(message = "상태 값은 비워둘 수 없습니다.")
    private String status; // 예: "SELLING", "RESERVED", "SOLD"

    /**
     * RESERVED 또는 SOLD로 전환할 때 지정되는 구매자 ID
     * (SELLING, DELETED로 바꿀 때는 null이어도 무방)
     */
    private Long buyerId; // ✅ 새로 추가됨
}
