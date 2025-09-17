package yongin.Yongnuri._Campus.dto.lostitem;

import java.time.LocalDateTime;

import lombok.Getter;
import yongin.Yongnuri._Campus.domain.LostItem;

@Getter
public class LostItemResponseDto {
    private final Long id;
    private final String title;
    private final String location;
    private final String purpose; 
    private final String status;  
    private final String content;
    private final Boolean isImages;
    private final LocalDateTime createdAt;
    private final String authorNickname; 

    
    public LostItemResponseDto(LostItem item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.content = item.getContent(); 
        this.location = item.getLocation();
        this.purpose = item.getPurpose().name(); 
        this.status = item.getStatus().name();
        
        // 임시처리
        this.isImages = false; 
        this.createdAt = null; 
        this.authorNickname = item.getUser().getNickName();
    }
}