package yongin.Yongnuri._Campus.dto.useditem;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequestDto {

    @NotBlank(message = "상태 값은 비워둘 수 없습니다.")
    private String status;
}