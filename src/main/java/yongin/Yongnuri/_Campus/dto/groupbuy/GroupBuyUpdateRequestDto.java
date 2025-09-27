package yongin.Yongnuri._Campus.dto.groupbuy;

import lombok.Getter;
import java.util.List;

@Getter
public class GroupBuyUpdateRequestDto {
    private String title;
    private String content;
    private String status;
    private String link;
    private Integer limit;
    private List<String> imageUrls;
}