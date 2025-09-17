package yongin.Yongnuri._Campus.dto.lostitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import yongin.Yongnuri._Campus.domain.LostItem;
import yongin.Yongnuri._Campus.domain.User;
import java.util.List;

@Getter
public class LostItemCreateRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "게시 목적(분실/습득)은 필수입니다.")
    private String purpose; 

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotBlank(message = "게시글 상태는 필수입니다.")
    private String status; 

    @NotBlank(message = "장소는 필수입니다.")
    private String location;

  
    private List<String> imageUrls;

    public LostItem toEntity(User user) {
        return LostItem.builder()
                .user(user) 
                .title(this.title)
                .content(this.content)
                .location(this.location)
                .purpose(LostItem.ItemPurpose.valueOf(this.purpose.toUpperCase()))
                .status(LostItem.ItemStatus.valueOf(this.status.toUpperCase()))
                .build();
    }
}