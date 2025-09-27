package yongin.Yongnuri._Campus.dto.groupbuy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import yongin.Yongnuri._Campus.domain.GroupBuy;
import yongin.Yongnuri._Campus.domain.User;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class GroupBuyCreateRequestDto {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    @NotBlank(message = "게시글상태는 필수입니다.")
    private String status;
    private String link;
    private Integer limit;
    private List<String> imageUrls;

    public GroupBuy toEntity(Long userId) {
        return GroupBuy.builder()
                .userId(userId)
                .title(this.title)
                .content(this.content)
                .status(GroupBuy.GroupBuyStatus.valueOf(this.status.toUpperCase()))
                .link(this.link)
                .limit(this.limit)
                .isImages(this.imageUrls != null && !this.imageUrls.isEmpty())
                .createdAt(LocalDateTime.now())
                .build();
    }
}