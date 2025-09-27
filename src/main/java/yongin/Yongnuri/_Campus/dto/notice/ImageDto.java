package yongin.Yongnuri._Campus.dto.notice;

import lombok.Getter;
import yongin.Yongnuri._Campus.domain.Image;

@Getter
public class ImageDto {
    private final String imageUrl;
    private final int sequence;
    public ImageDto(Image image) {
        this.imageUrl = image.getImageUrl();
        this.sequence = image.getSequence();
    }
}