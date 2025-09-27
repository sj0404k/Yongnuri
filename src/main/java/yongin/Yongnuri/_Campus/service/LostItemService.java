package yongin.Yongnuri._Campus.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import yongin.Yongnuri._Campus.domain.Bookmark;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.LostItem;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemCreateRequestDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemResponseDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemUpdateRequestDto;
import yongin.Yongnuri._Campus.repository.BookmarkRepository;
import yongin.Yongnuri._Campus.repository.ImageRepository;
import yongin.Yongnuri._Campus.repository.LostItemRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class LostItemService {

    private final LostItemRepository lostItemRepository;
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

    /*
     분실물 게시글 목록 조회 
    */
    public List<LostItemResponseDto> getLostItems(String email, String type) {
        User currentUser = getUserByEmail(email);
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUser.getId());

        Specification<LostItem> spec = (root, query, cb) -> {
            return cb.not(root.get("user").get("id").in(blockedUserIds));
        };
        spec = spec.and((root, query, cb) -> cb.notEqual(root.get("status"), LostItem.ItemStatus.DELETED));
        if (!"전체".equals(type)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("location"), type));
        }
        List<LostItem> items = lostItemRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"));
        if (items.isEmpty()) {
            return List.of();
        }


        List<Long> postIds = items.stream().map(LostItem::getId).collect(Collectors.toList());

        List<Bookmark> myBookmarks = bookmarkRepository.findByUserIdAndPostTypeAndPostIdIn(
                currentUser.getId(), "LOST_ITEM", postIds);
        Set<Long> myBookmarkedPostIds = myBookmarks.stream()
                .map(Bookmark::getPostId).collect(Collectors.toSet());

        List<Long> postIdsWithImages = items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsImages()))
                .map(LostItem::getId)
                .collect(Collectors.toList());
        List<Image> thumbnails = imageRepository.findByTypeAndTypeIdInAndSequence("LOST_ITEM", postIdsWithImages, 1);
        Map<Long, String> thumbnailMap = thumbnails.stream()
                .collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));

        return items.stream()
                .map(item -> {
                    LostItemResponseDto dto = new LostItemResponseDto(item);
                    dto.setThumbnailUrl(thumbnailMap.get(item.getId()));
                    dto.setBookmarked(myBookmarkedPostIds.contains(item.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    //상세조회
    public LostItemResponseDto getLostItemDetail(String email, Long postId) {
        User currentUser = getUserByEmail(email);
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUser.getId());

        LostItem item = lostItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));
        if (item.getStatus() == LostItem.ItemStatus.DELETED) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }
        if (blockedUserIds.contains(item.getUser().getId())) {
            throw new EntityNotFoundException("404: 게시글 없음 (차단됨)");
        }
        List<Image> images = List.of();
        if (Boolean.TRUE.equals(item.getIsImages())) {
            images = imageRepository.findByTypeAndTypeIdOrderBySequenceAsc("LOST_ITEM", postId);
        }

        LostItemResponseDto dto = new LostItemResponseDto(item, images);

        boolean isBookmarked = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(
                currentUser.getId(), "LOST_ITEM", postId);
        dto.setBookmarked(isBookmarked);

        return dto;
    }
    /* 
     분실물 게시글 작성
     */
    @Transactional
    public Long createLostItem(String email, LostItemCreateRequestDto requestDto) {
        User currentUser = getUserByEmail(email);
        LostItem newItem = requestDto.toEntity(currentUser);

        LostItem savedItem = lostItemRepository.save(newItem);
        Long newPostId = savedItem.getId();
        List<String> imageUrls = requestDto.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            int sequence = 1;
            for (String url : imageUrls) {
                Image image = Image.builder()
                        .type("LOST_ITEM")
                        .typeId(newPostId)
                        .imageUrl(url)
                        .sequence(sequence++)
                        .build();
                imageRepository.save(image);
            }
        }


        return newPostId;
    }
     /*
     분실물 게시글 수정
     */
     @Transactional
     public Long updateLostItem(String email, Long postId, LostItemUpdateRequestDto requestDto) {
         User currentUser = getUserByEmail(email);
         LostItem item = lostItemRepository.findById(postId)
                 .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));
         if (!item.getUser().getId().equals(currentUser.getId())) {
             throw new AccessDeniedException("403: 이 게시글을 수정할 권한이 없습니다.");
         }
         if (requestDto.getImageUrls() != null) {
             imageRepository.deleteAllByTypeAndTypeId("LOST_ITEM", postId);
             List<String> imageUrls = requestDto.getImageUrls();
             int sequence = 1;
             for (String url : imageUrls) {
                 Image image = Image.builder()
                         .type("LOST_ITEM")
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
         if (requestDto.getPurpose() != null) {
             item.setPurpose(LostItem.ItemPurpose.valueOf(requestDto.getPurpose().toUpperCase()));
         }
         if (requestDto.getStatus() != null) {
             item.setStatus(LostItem.ItemStatus.valueOf(requestDto.getStatus().toUpperCase()));
         }
         return item.getId();
     }
}