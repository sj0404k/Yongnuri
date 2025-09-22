package yongin.Yongnuri._Campus.service;

import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.domain.Bookmark;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemCreateRequestDto;
import yongin.Yongnuri._Campus.repository.BookmarkRepository;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkCountDto;
import yongin.Yongnuri._Campus.repository.ImageRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.repository.UsedItemRepository;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemResponseDto;
import yongin.Yongnuri._Campus.service.specification.BoardSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException; 
import yongin.Yongnuri._Campus.dto.useditem.UsedItemUpdateRequestDto;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsedItemService {

    private final UsedItemRepository usedItemRepository;
    private final BlockService blockService;
    private final UserRepository userRepository; 
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }
    //게시글목록조회
    public List<UsedItemResponseDto> getUsedItems(String email, String type) {
        User currentUser = getUserByEmail(email);
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUser.getId());
        Specification<UsedItem> spec = BoardSpecification.notBlocked(blockedUserIds);
        if (!"전체".equals(type)) {
            spec = spec.and(BoardSpecification.hasLocation(type));
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<UsedItem> items = usedItemRepository.findAll(spec, sort);
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> postIds = items.stream().map(UsedItem::getId).collect(Collectors.toList());

        List<Bookmark> myBookmarks = bookmarkRepository.findByUserIdAndPostTypeAndPostIdIn(currentUser.getId(), "USED_ITEM", postIds);
        Set<Long> myBookmarkedPostIds = myBookmarks.stream().map(Bookmark::getPostId).collect(Collectors.toSet());

        List<BookmarkCountDto> bookmarkCounts = bookmarkRepository.countByPostTypeAndPostIdIn("USED_ITEM", postIds);
        Map<Long, Long> bookmarkCountMap = bookmarkCounts.stream()
                .collect(Collectors.toMap(BookmarkCountDto::getPostId, BookmarkCountDto::getCount));

        List<Long> postIdsWithImages = items.stream().filter(UsedItem::getIsImages).map(UsedItem::getId).collect(Collectors.toList());
        List<Image> thumbnails = imageRepository.findByTypeAndTypeIdInAndSequence("USED_ITEM", postIdsWithImages, 1);
        Map<Long, String> thumbnailMap = thumbnails.stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));

        return items.stream()
                .map(item -> {
                    UsedItemResponseDto dto = new UsedItemResponseDto(item);
                    dto.setThumbnailUrl(thumbnailMap.get(item.getId()));
                    dto.setBookmarked(myBookmarkedPostIds.contains(item.getId()));
                    dto.setBookmarkCount(bookmarkCountMap.getOrDefault(item.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회 
    public UsedItemResponseDto getUsedItemDetail(String email, Long postId) {
        User currentUser = getUserByEmail(email);
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUser.getId());
        UsedItem item = usedItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));
        Long authorId = item.getUserId();
        if (blockedUserIds.contains(authorId)) {
            throw new EntityNotFoundException("404: 게시글 없음 (차단됨)"); 
        }
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("작성자 정보를 찾을 수 없습니다."));
        List<Image> images = List.of();
        if (Boolean.TRUE.equals(item.getIsImages())) {
            images = imageRepository.findByTypeAndTypeIdOrderBySequenceAsc("USED_ITEM", postId);
        }

        UsedItemResponseDto dto = new UsedItemResponseDto(item, author, images);
        boolean isBookmarked = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(currentUser.getId(), "USED_ITEM", postId);
        dto.setBookmarked(isBookmarked);

        return dto;
    }

    /*
     중고 게시글 작성
     */
    @Transactional
    public Long createUsedItem(String email, UsedItemCreateRequestDto requestDto) {
        Long currentUserId = getUserByEmail(email).getId();

        UsedItem newItem = requestDto.toEntity(currentUserId);

        UsedItem savedItem = usedItemRepository.save(newItem);
        Long newPostId = savedItem.getId();

        List<String> imageUrls = requestDto.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            int sequence = 1;
            for (String url : imageUrls) {
                Image image = Image.builder()
                        .type("USED_ITEM")
                        .typeId(newPostId)
                        .imageUrl(url)
                        .sequence(sequence++)
                        .build();
                imageRepository.save(image);
            }
        }
        return newPostId;
    }
    @Transactional 
    public Long updateUsedItem(String email, Long postId, UsedItemUpdateRequestDto requestDto) {
        
      
        Long currentUserId = getUserByEmail(email).getId();

       
        UsedItem item = usedItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));

       
        if (!item.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("403: 이 게시글을 수정할 권한이 없습니다.");
        }

        if (requestDto.getImageUrls() != null) {
            imageRepository.deleteAllByTypeAndTypeId("USED_ITEM", postId);
            List<String> imageUrls = requestDto.getImageUrls();
            int sequence = 1;
            for (String url : imageUrls) {
                Image image = Image.builder()
                        .type("USED_ITEM")
                        .typeId(postId)
                        .imageUrl(url)
                        .sequence(sequence++)
                        .build();
                imageRepository.save(image);
            }
            item.setIsImages(!imageUrls.isEmpty());
        }
        if (requestDto.getTitle() != null) {
            item.setTitle(requestDto.getTitle());
        }
        if (requestDto.getContent() != null) {
            item.setContent(requestDto.getContent());
        }
        if (requestDto.getLocation() != null) {
            item.setLocation(requestDto.getLocation());
        }
        if (requestDto.getMethod() != null) {
            item.setMethod(requestDto.getMethod());
        }
        if (requestDto.getStatus() != null) {
            item.setStatus(requestDto.getStatus());
        }
        if (requestDto.getPrice() != null) {
            item.setPrice(requestDto.getPrice());
        }
        return item.getId();
    }
}

