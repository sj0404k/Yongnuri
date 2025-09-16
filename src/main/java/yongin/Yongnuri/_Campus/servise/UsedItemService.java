/*// servise/UsedItemService.java
package yongin.Yongnuri._Campus.servise;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification; // 공통 스펙 임포트
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemResponseDto;
import yongin.Yongnuri._Campus.repository.UsedItemRepository;
import yongin.Yongnuri._Campus.servise.specification.BoardSpecification;

@Service
@RequiredArgsConstructor
public class UsedItemService {

    private final UsedItemRepository usedItemRepository;
    private final BlockService blockService; 

    public List<UsedItemResponseDto> getUsedItems(Long currentUserId, String type) {

        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUserId);

        // (수정) 'Specification.where(null)'로 시작하는 대신,
        // 첫 번째 스펙을 바로 받아서 시작합니다. (이러면 경고가 사라집니다)
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
}
// servise/UsedItemService.java
package yongin.Yongnuri._Campus.servise;

import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.repository.UserRepository; // <--- 1. UserRepository 임포트
import yongin.Yongnuri._Campus.repository.UsedItemRepository;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemResponseDto;
import yongin.Yongnuri._Campus.servise.specification.BoardSpecification;
import jakarta.persistence.EntityNotFoundException; // <--- 404 처리를 위해 임포트

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsedItemService {

    private final UsedItemRepository usedItemRepository;
    private final BlockService blockService;
    private final UserRepository userRepository; // <--- 2. UserRepository 주입받기

    // (Helper) email로 User 객체 찾는 내부 메서드
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }

    /**
     * (수정) 중고 게시글 목록 조회
     * Controller에서 email을 직접 받도록 수정
     
    public List<UsedItemResponseDto> getUsedItems(String email, String type) {
        Long currentUserId = getUserByEmail(email).getId(); // 1. email로 User ID 찾기

        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUserId);

        Specification<UsedItem> spec = BoardSpecification.notBlocked(blockedUserIds);

        if (!"전체".equals(type)) {
            spec = spec.and(BoardSpecification.hasLocation(type));
        }
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<UsedItem> items = usedItemRepository.findAll(spec, sort);

        // DTO 리스트용 생성자(UsedItemResponseDto(item)) 호출
        return items.stream()
                .map(UsedItemResponseDto::new) 
                .collect(Collectors.toList());
    }

    /**
     * (추가) 중고 게시글 상세 조회
     
    public UsedItemResponseDto getUsedItemDetail(String email, Long postId) {
        Long currentUserId = getUserByEmail(email).getId(); // 1. email로 User ID 찾기
        
        // 2. 내가 차단한 유저 ID 목록 조회
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUserId);

        // 3. 게시글 ID로 게시글 조회 (없으면 404)
        UsedItem item = usedItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));

        // 4. [차단 로직] 해당 게시글 작성자가 내 차단 목록에 있는지 확인
        Long authorId = item.getUserId();
        if (blockedUserIds.contains(authorId)) {
            // 차단한 유저의 글이면, 동일하게 404로 응답 (게시글이 없는 척)
            throw new EntityNotFoundException("404: 게시글 없음 (차단됨)"); 
        }

        // 5. "글 작성자" 정보 (User) 조회
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("작성자 정보를 찾을 수 없습니다. (탈퇴한 회원)"));

        // 6. DTO 상세조회용 생성자(UsedItemResponseDto(item, author)) 호출
        return new UsedItemResponseDto(item, author);
    }
}
*/
// servise/UsedItemService.java

package yongin.Yongnuri._Campus.servise;

import yongin.Yongnuri._Campus.domain.Image; // (추가)
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemCreateRequestDto; // (추가)
import yongin.Yongnuri._Campus.repository.ImageRepository; // (추가)
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.repository.UsedItemRepository;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemResponseDto;
import yongin.Yongnuri._Campus.servise.specification.BoardSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional; // (추가)

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException; // 403 권한 예외
import yongin.Yongnuri._Campus.dto.useditem.UsedItemUpdateRequestDto; // (추가)

@Service
@RequiredArgsConstructor
public class UsedItemService {

    private final UsedItemRepository usedItemRepository;
    private final BlockService blockService;
    private final UserRepository userRepository; 
    private final ImageRepository imageRepository; // <--- 1. ImageRepository 주입

    // (Helper 메서드 - 기존과 동일)
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }

    // (게시글 목록 조회 - 기존과 동일)
    public List<UsedItemResponseDto> getUsedItems(String email, String type) {
        // ... (기존 코드 생략) ...
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

    // (게시글 상세 조회 - 기존과 동일)
    public UsedItemResponseDto getUsedItemDetail(String email, Long postId) {
        // ... (기존 코드 생략) ...
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

    /**
     * (추가/수정) 중고 게시글 작성 (이미지 저장 로직 포함)
     */
    @Transactional // DB에 두 종류의 저장을 하므로 트랜잭션 필수
    public Long createUsedItem(String email, UsedItemCreateRequestDto requestDto) {
        
        Long currentUserId = getUserByEmail(email).getId();
        UsedItem newItem = requestDto.toEntity(currentUserId);

        // 1. 게시글 본문을 먼저 저장 (게시글 ID 생성)
        UsedItem savedItem = usedItemRepository.save(newItem);
        Long newPostId = savedItem.getId();

        // 2. 이미지 URL 리스트가 있다면, Image 테이블에 저장
        List<String> imageUrls = requestDto.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            
            int sequence = 1; // 이미지 순서
            for (String url : imageUrls) {
                Image image = Image.builder()
                        .type("USED_ITEM") // (타입: 중고거래)
                        .typeId(newPostId) // (방금 생성된 게시글 ID)
                        .imageUrl(url)
                        .sequence(sequence++) // 순서 (1, 2, 3...)
                        .build();
                
                imageRepository.save(image); // 이미지 저장
            }
        }

        // 3. 생성된 게시글의 ID 반환
        return newPostId;
    }
    @Transactional // 여러 DB 작업을 하므로 트랜잭션 필수
    public Long updateUsedItem(String email, Long postId, UsedItemUpdateRequestDto requestDto) {
        
        // 1. 현재 유저 정보 가져오기
        Long currentUserId = getUserByEmail(email).getId();

        // 2. [404] 게시글 조회 (없으면 404)
        UsedItem item = usedItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));

        // 3. [403] 권한 확인 (게시글 작성자와 현재 유저가 동일한지)
        if (!item.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("403: 이 게시글을 수정할 권한이 없습니다.");
        }

        // 4. (변경) 이미지 목록 업데이트 (핵심 로직)
        // DTO에 imageUrls 필드가 (null이 아닌) 상태로 왔다면, 이미지 목록을 '전체 교체'합니다.
        if (requestDto.getImageUrls() != null) {
            // (1) 기존 이미지 모두 삭제
            imageRepository.deleteAllByTypeAndTypeId("USED_ITEM", postId);

            // (2) 새 이미지 리스트 저장
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
            
            // (3) UsedItem의 isImages 플래그 업데이트
            item.setIsImages(!imageUrls.isEmpty());
        }

        // 5. (변경) 나머지 텍스트 정보 업데이트 (null이 아닌 필드만)
        // (UsedItem Entity에 @Setter가 있거나, update 헬퍼 메서드가 필요합니다.)
        // (가장 간단한 @Setter를 UsedItem Entity에 추가했다고 가정)
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
        
        // 6. (JPA 더티 체킹) @Transactional이 종료되면서 변경된 'item'이 자동으로 DB에 저장(UPDATE)됨
        // (명시적으로 usedItemRepository.save(item);을 호출해도 좋습니다.)

        return item.getId();
    }
}

