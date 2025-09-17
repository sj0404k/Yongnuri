package yongin.Yongnuri._Campus.dto.lostitem;

import lombok.Getter;
import java.util.List;

@Getter
public class LostItemUpdateRequestDto {
    private String title;
    private String purpose; 
    private String content;
    private String status; 
    private String location;
    private List<String> imageUrls; 
}