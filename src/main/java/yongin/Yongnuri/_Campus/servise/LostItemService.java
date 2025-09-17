package yongin.Yongnuri._Campus.servise;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.LostItem;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemCreateRequestDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemResponseDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemUpdateRequestDto;
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

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }

    /*
     분실물 게시글 목록 조회 
    */
    public List<LostItemResponseDto> getLostItems(String email, String type) {
        Long currentUserId = getUserByEmail(email).getId();
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUserId);
        List<LostItem> allItems = lostItemRepository.findAll(Sort.by(Sort.Direction.DESC, "id")); 
        Stream<LostItem> stream = allItems.stream()
                .filter(item -> !blockedUserIds.contains(item.getUser().getId()));
        if (!"전체".equals(type)) {
            stream = stream.filter(item -> type.equals(item.getLocation()));
        }

        return stream
                .map(LostItemResponseDto::new)
                .collect(Collectors.toList());
    }
    //상세조회
    public LostItemResponseDto getLostItemDetail(String email, Long postId) {
        Long currentUserId = getUserByEmail(email).getId();
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUserId);
        LostItem item = lostItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));
        Long authorId = item.getUser().getId();
        if (blockedUserIds.contains(authorId)) {
            throw new EntityNotFoundException("404: 게시글 없음 (차단됨)");
        }
        return new LostItemResponseDto(item);
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
        }

        if (requestDto.getTitle() != null) item.setTitle(requestDto.getTitle());
        if (requestDto.getContent() != null) item.setContent(requestDto.getContent());
        if (requestDto.getLocation() != null) item.setLocation(requestDto.getLocation());
        if (requestDto.getPurpose() != null) item.setPurpose(LostItem.ItemPurpose.valueOf(requestDto.getPurpose().toUpperCase()));
        if (requestDto.getStatus() != null) item.setStatus(LostItem.ItemStatus.valueOf(requestDto.getStatus().toUpperCase()));
        
        return item.getId();
    }
}