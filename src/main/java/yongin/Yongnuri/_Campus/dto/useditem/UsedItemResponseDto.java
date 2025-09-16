package yongin.Yongnuri._Campus.dto.useditem;

import lombok.Getter;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.domain.User;

@Getter
public class UsedItemResponseDto {
    
    private Long id;
    private String title;
    private String content; 
    private int price;
    private String location;
    private String status;
    private String authorNickname; 

    // 1. 리스트 조회용 생성자 
    // (중고거래 목록 조회 사용)
    public UsedItemResponseDto(UsedItem item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.status = item.getStatus();
        this.content = null; 
        this.authorNickname = null; 
    }

    // 2. 상세 조회용 생성자 
    // (중고거래게시판 상세 조회 시 사용)
    public UsedItemResponseDto(UsedItem item, User author) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.content = item.getContent(); 
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.status = item.getStatus();
        this.authorNickname = (author != null) ? author.getNickName() : "(알 수 없음)";
    }
}