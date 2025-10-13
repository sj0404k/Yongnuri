package yongin.Yongnuri._Campus.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkCountDto;
import yongin.Yongnuri._Campus.dto.groupbuy.GroupBuyCreateRequestDto;
import yongin.Yongnuri._Campus.dto.groupbuy.GroupBuyResponseDto;
import yongin.Yongnuri._Campus.dto.groupbuy.GroupBuyUpdateRequestDto;
import yongin.Yongnuri._Campus.dto.groupbuy.UpdateCountRequestDto;
import yongin.Yongnuri._Campus.exception.ConflictException;
import yongin.Yongnuri._Campus.repository.*;
import yongin.Yongnuri._Campus.service.specification.BoardSpecification;
import yongin.Yongnuri._Campus.domain.Enum;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import yongin.Yongnuri._Campus.domain.Bookmark;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.repository.BookmarkRepository;
import yongin.Yongnuri._Campus.repository.ImageRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class GroupBuyService {
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyApplicantRepository applicantRepository;
    private final BlockService blockService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }
//공동구매목록
    public List<GroupBuyResponseDto> getGroupBuys(String email, String type) {
        User currentUser = getUserByEmail(email);
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUser.getId());

        Specification<GroupBuy> spec = (root, query, cb) -> cb.notEqual(root.get("status"), Enum.GroupBuyStatus.DELETED);
        spec = spec.and(BoardSpecification.notBlocked(blockedUserIds));
        List<GroupBuy> items = groupBuyRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (items.isEmpty()) return List.of();

        List<Long> postIds = items.stream().map(GroupBuy::getId).collect(Collectors.toList());

        Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("GROUP_BUY", postIds, 1)
                .stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));

        Set<Long> myBookmarkedPostIds = bookmarkRepository.findByUserIdAndPostTypeAndPostIdIn(currentUser.getId(), "GROUP_BUY", postIds)
                .stream().map(Bookmark::getPostId).collect(Collectors.toSet());
        List<BookmarkCountDto> bookmarkCounts = bookmarkRepository.findBookmarkCountsByPostTypeAndPostIdIn("GROUP_BUY", postIds);
        Map<Long, Long> bookmarkCountMap = bookmarkCounts.stream()
                .collect(Collectors.toMap(BookmarkCountDto::getPostId, BookmarkCountDto::getCount));

        return items.stream().map(item -> {
            GroupBuyResponseDto dto = new GroupBuyResponseDto(item);
            dto.setThumbnailUrl(thumbnailMap.get(item.getId()));
            dto.setBookmarked(myBookmarkedPostIds.contains(item.getId()));
            dto.setBookmarkCount(bookmarkCountMap.getOrDefault(item.getId(), 0L));
            return dto;
        }).collect(Collectors.toList());
    }
//공동구매상세보기
    public GroupBuyResponseDto getGroupBuyDetail(String email, Long postId) {
        User currentUser = getUserByEmail(email);

        GroupBuy item = groupBuyRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));
        if (item.getStatus() == Enum.GroupBuyStatus.DELETED) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }
        if (blockService.getBlockedUserIds(currentUser.getId()).contains(item.getUserId())) {
            throw new EntityNotFoundException("404: 게시글 없음 (차단됨)");
        }
        User author = userRepository.findById(item.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("작성자 정보를 찾을 수 없습니다."));

        List<Image> images = imageRepository.findByTypeAndTypeIdOrderBySequenceAsc("GROUP_BUY", postId);
        boolean isBookmarked = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(currentUser.getId(), "GROUP_BUY", postId);

        GroupBuyResponseDto dto = new GroupBuyResponseDto(item, author, images);
        dto.setBookmarked(isBookmarked);
        long bookmarkCount = bookmarkRepository.countByPostTypeAndPostId("GROUP_BUY", postId);
        dto.setBookmarkCount(bookmarkCount);

        return dto;
    }
    //공동구매글작성
    @Transactional
    public Long createGroupBuy(String email, GroupBuyCreateRequestDto requestDto) {
        Long currentUserId = getUserByEmail(email).getId();
        GroupBuy newItem = requestDto.toEntity(currentUserId);
        GroupBuy savedItem = groupBuyRepository.save(newItem);
        Long newPostId = savedItem.getId();

        List<String> imageUrls = requestDto.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            int sequence = 1;
            for (String url : imageUrls) {
                imageRepository.save(Image.builder()
                        .type("GROUP_BUY").typeId(newPostId).imageUrl(url).sequence(sequence++).build());
            }
        }
        return newPostId;
    }

    //공동구매글수정
    @Transactional
    public Long updateGroupBuy(String email, Long postId, GroupBuyUpdateRequestDto requestDto) {
        Long currentUserId = getUserByEmail(email).getId();
        GroupBuy item = groupBuyRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));

        if (!item.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("403: 이 게시글을 수정할 권한이 없습니다.");
        }

        if (requestDto.getImageUrls() != null) {
            imageRepository.deleteAllByTypeAndTypeId("GROUP_BUY", postId);
            int sequence = 1;
            for (String url : requestDto.getImageUrls()) {
                imageRepository.save(Image.builder()
                        .type("GROUP_BUY").typeId(postId).imageUrl(url).sequence(sequence++).build());
            }
            item.setIsImages(!requestDto.getImageUrls().isEmpty());
        }

        if (requestDto.getTitle() != null) item.setTitle(requestDto.getTitle());
        if (requestDto.getContent() != null) item.setContent(requestDto.getContent());
        if (requestDto.getStatus() != null) {item.setStatus(Enum.GroupBuyStatus.valueOf(requestDto.getStatus().toUpperCase()));}
        if (requestDto.getLink() != null) item.setLink(requestDto.getLink());
        if (requestDto.getLimit() != null) item.setLimit(requestDto.getLimit());
        return item.getId();
    }
     //공동구매 신청하기
    @Transactional
    public void applyForGroupBuy(String email, Long postId) {
        User currentUser = getUserByEmail(email);
        GroupBuy item = groupBuyRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공동구매 게시글입니다."));

        if (item.getUserId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("자신이 작성한 게시글에는 신청할 수 없습니다.");
        }
        boolean alreadyApplied = applicantRepository.existsByPostIdAndUserId(postId, currentUser.getId());
        if (alreadyApplied) {
            throw new ConflictException("이미 신청한 공동구매입니다.");
        }

        Integer limit = item.getLimit();
        if (limit != null && limit > 0) {
            long currentCount = applicantRepository.countByPostId(postId);
            if (currentCount >= limit) {
                throw new ConflictException("모집 인원이 마감되었습니다.");
            }
        }
        GroupBuyApplicant newApplicant = GroupBuyApplicant.builder()
                .postId(postId)
                .userId(currentUser.getId())
                .status("APPLIED")
                .createdAt(LocalDateTime.now())
                .build();

        applicantRepository.save(newApplicant);
    }

      //현재 모집 인원 수정
      @Transactional
      public GroupBuyResponseDto updateCurrentCount(String email, Long postId, UpdateCountRequestDto requestDto) {
          User currentUser = getUserByEmail(email);
        GroupBuy item = groupBuyRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));
        if (!item.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("403: 인원을 수정할 권한이 없습니다.");
        }
        Integer newCount = requestDto.getCount();
        Integer limit = item.getLimit();
        if (limit != null && limit > 0 && newCount > limit) {
            throw new IllegalArgumentException("현재 인원은 전체 인원 제한(" + limit + "명)을 초과할 수 없습니다.");
        }
        if (newCount < 0) {
            throw new IllegalArgumentException("현재 인원은 0명 미만이 될 수 없습니다.");
        }
        item.setCurrentCount(requestDto.getCount());

        groupBuyRepository.flush();

        GroupBuy updatedItem = groupBuyRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));

        return new GroupBuyResponseDto(updatedItem);
    }
}