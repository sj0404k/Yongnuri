/*// dto/useditem/UsedItemResponseDto.java
package yongin.Yongnuri._Campus.dto.useditem;

import lombok.Getter;
import yongin.Yongnuri._Campus.domain.UsedItem;

@Getter
public class UsedItemResponseDto {
    
    private Long id;
    private String title;
    private int price;
    private String location;
    private String status;
    // ... (응답으로 클라이언트에게 보여줄 필드들)
    
    // Entity를 DTO로 변환하는 생성자
    public UsedItemResponseDto(UsedItem item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.status = item.getStatus();
        // ...
    }
} */
// dto/useditem/UsedItemResponseDto.java
package yongin.Yongnuri._Campus.dto.useditem;

import lombok.Getter;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.domain.User;

@Getter
public class UsedItemResponseDto {
    
    private Long id;
    private String title;
    private String content; // (상세) 글 내용
    private int price;
    private String location;
    private String status;
    private String authorNickname; // (상세) "글 작성자" 닉네임

    // 1. 리스트 조회용 생성자 (간단 버전)
    // (getUsedItems - 목록 조회 시 사용)
    public UsedItemResponseDto(UsedItem item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.status = item.getStatus();
        this.content = null; // 리스트에서는 content 제외
        this.authorNickname = null; // 리스트에서는 작성자 닉네임 제외 (필요하면 추가)
    }

    // 2. 상세 조회용 생성자 (풀 버전)
    // (getUsedItemDetail - 상세 조회 시 사용)
    public UsedItemResponseDto(UsedItem item, User author) {
        this.id = item.getId();
        this.title = item.getTitle();
        // (가정) UsedItem Entity에 getContent() 메서드가 있다고 가정
        this.content = item.getContent(); 
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.status = item.getStatus();
        this.authorNickname = (author != null) ? author.getNickName() : "(알 수 없음)";
    }
}