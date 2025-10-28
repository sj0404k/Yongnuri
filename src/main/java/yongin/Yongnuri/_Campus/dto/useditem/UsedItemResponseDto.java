package yongin.Yongnuri._Campus.dto.useditem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.domain.User;

/**
 * 중고거래 응답 DTO
 * - 목록 조회용 생성자: 최소 필드만 채우고, content/images 등은 null
 * - 상세 조회용 생성자: 작성자/이미지까지 모두 채움
 * - 썸네일/북마크 정보는 서비스단에서 set* 로 주입
 */
@Getter
public class UsedItemResponseDto {

    private Long id;
    private String title;
    private String content;                 // 상세에서 사용
    private int price;
    private String location;
    private String status;

    private Enum.ChatType Type;
    private Long userId;            //글 작성자

    private String authorNickname;          // 작성자 표시 이름
    private String authorDepartment;        // 작성자 학과(=User.major)
    private String authorEmail;             // ✅ 오너 판정용
    private Enum.authStatus authorStatus;            // 작성자 상태
    private LocalDateTime createdAt;

    private List<ImageDto> images;          // 상세에서만 채움

    @Setter
    private String thumbnailUrl;            // 목록에서 1번 이미지 경로

    @Setter
    private boolean isBookmarked;           // 나의 북마크 여부

    @Setter
    private Long bookmarkCount;             // 북마크 총 개수

    /** ✅ 목록 조회용 (최소 필드만 세팅) */
    public UsedItemResponseDto(UsedItem item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.status = item.getStatus().name();
        this.createdAt = item.getCreatedAt();

        // 목록에서는 아래 필드는 비움(서비스에서 필요 시 setter로 주입)
        this.content = null;
        this.images = null;
        this.authorNickname = null;
        this.authorDepartment = null;
        this.authorEmail = null;
        this.authorStatus=null;
        this.thumbnailUrl = null;
        this.isBookmarked = false;
        this.bookmarkCount = 0L;
    }

    /** ✅ 상세 조회용 (작성자/이미지 포함) */
    public UsedItemResponseDto(UsedItem item, User author, List<Image> images) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.content = item.getContent();
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.status = item.getStatus().name();
        this.createdAt = item.getCreatedAt();

        this.Type = Enum.ChatType.USED_ITEM;
        this.userId = author.getId();

        // 작성자 정보: User 엔티티 필드명에 맞춰 매핑
        this.authorNickname   = (author != null) ? author.getNickName() : "(알 수 없음)";
        this.authorDepartment = (author != null) ? author.getMajor()   : null;   // ← User.major
        this.authorEmail      = (author != null) ? author.getEmail()   : null;
        this.authorStatus= Objects.requireNonNull(author).getStatus();
        // 이미지 시퀀스 순으로 매핑
        this.images = (images != null)
                ? images.stream().map(ImageDto::new).collect(Collectors.toList())
                : null;

        // 상세 기본값
        this.isBookmarked = false;
        this.bookmarkCount = 0L;
        // thumbnailUrl은 상세에선 보통 사용 안 하지만, 필요 시 서비스에서 set 가능
        this.thumbnailUrl = null;
    }
}
