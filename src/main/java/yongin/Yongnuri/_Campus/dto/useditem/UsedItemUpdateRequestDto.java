package yongin.Yongnuri._Campus.dto.useditem;

import java.util.List;

import lombok.Getter;


@Getter
public class UsedItemUpdateRequestDto {

    private String title;
    private String content;
    private String location;
    private String method;
    private String status; 
    private Integer price;
    private List<String> imageUrls; 

}