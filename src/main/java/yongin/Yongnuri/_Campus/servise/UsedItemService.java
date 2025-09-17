package yongin.Yongnuri._Campus.servise;

import yongin.Yongnuri._Campus.domain.Image; 
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemCreateRequestDto; 
import yongin.Yongnuri._Campus.repository.ImageRepository; 
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.repository.UsedItemRepository;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemResponseDto;
import yongin.Yongnuri._Campus.servise.specification.BoardSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional; 

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException; 
import yongin.Yongnuri._Campus.dto.useditem.UsedItemUpdateRequestDto; 

@Service
@RequiredArgsConstructor
public class UsedItemService {

    private final UsedItemRepository usedItemRepository;
    private final BlockService blockService;
    private final UserRepository userRepository; 
    private final ImageRepository imageRepository; 


    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }

    // 게시글 목록 조회
    public List<UsedItemResponseDto> getUsedItems(String email, String type) { 
        Long currentUserId = getUserByEmail(email).getId();
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUserId);
        Specification<UsedItem> spec = BoardSpecification.notBlocked(blockedUserIds);
        if (!"전체".equals(type)) {
            spec = spec.and(BoardSpecification.hasLocation(type));
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<UsedItem> items = usedItemRepository.findAll(spec, sort);
        return items.stream()
                .map(UsedItemResponseDto::new) 
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회 
    public UsedItemResponseDto getUsedItemDetail(String email, Long postId) {
        Long currentUserId = getUserByEmail(email).getId();
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUserId);
        UsedItem item = usedItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));
        Long authorId = item.getUserId();
        if (blockedUserIds.contains(authorId)) {
            throw new EntityNotFoundException("404: 게시글 없음 (차단됨)"); 
        }
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("작성자 정보를 찾을 수 없습니다."));
        return new UsedItemResponseDto(item, author);
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

