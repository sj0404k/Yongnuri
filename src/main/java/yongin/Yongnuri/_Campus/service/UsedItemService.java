package yongin.Yongnuri._Campus.service;

import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.dto.NotificationRequest;
import yongin.Yongnuri._Campus.dto.useditem.UpdateStatusRequestDto;
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

import java.time.LocalDateTime;
import java.util.List;
import yongin.Yongnuri._Campus.repository.AppointmentRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemUpdateRequestDto;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import yongin.Yongnuri._Campus.domain.Enum;

@Service
@RequiredArgsConstructor
public class UsedItemService {

    private final UsedItemRepository usedItemRepository;
    private final BlockService blockService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
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
        Specification<UsedItem> spec = (root, query, cb) ->
                cb.notEqual(root.get("status"), Enum.UsedItemStatus.DELETED);
        spec = spec.and(BoardSpecification.notBlocked(blockedUserIds));
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

        List<BookmarkCountDto> bookmarkCounts = bookmarkRepository.findBookmarkCountsByPostTypeAndPostIdIn("USED_ITEM", postIds);
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
        if (item.getStatus() == Enum.UsedItemStatus.DELETED) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }
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
        long bookmarkCount = bookmarkRepository.countByPostTypeAndPostId("USED_ITEM", postId);
        dto.setBookmarkCount(bookmarkCount);
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
    //게시글수정
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
            item.setStatus(Enum.UsedItemStatus.valueOf(requestDto.getStatus().toUpperCase()));
        }
        if (requestDto.getPrice() != null) {
            item.setPrice(requestDto.getPrice());
        }
        return item.getId();
    }

    //중고거래 게시글 상태 변경

    @Transactional
    public void updateUsedItemStatus(String email, Long postId, UpdateStatusRequestDto requestDto) {
        // 1) 권한/엔티티 확인 -------------------------------------------------------
        User currentUser = getUserByEmail(email);
        UsedItem item = usedItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));

        if (!item.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("403: 상태를 변경할 권한이 없습니다.");
        }

        // 2) 상태 파싱 (SELLING / RESERVED / SOLD / DELETED) -----------------------
        Enum.UsedItemStatus newStatus = Enum.UsedItemStatus.valueOf(
                requestDto.getStatus().toUpperCase()
        );

        // 3) 상태 적용
        item.setStatus(newStatus);

        // 4) 해당 글과 연관된 약속(appointment) 조회 -------------------------------
        //    - 구매 탭 노출은 Appointment(buyerId, status) 기반으로 이뤄지므로
        //      상태 변경 시 Appointment를 반드시 동기화해야 함.
        List<Appointment> appointments = appointmentRepository.findByPostTypeAndPostId("USED_ITEM", postId);

        // 프론트에서 넘어온 구매자 ID (RESERVED/SOLD에 필요)
        Long buyerId = requestDto.getBuyerId();

        // 5) 상태별 Appointment 동기화 ---------------------------------------------
        switch (newStatus) {
            case RESERVED: {
                // ✅ 예약중: 특정 구매자에게 예약을 걸어야 하므로 buyerId 필요
                if (buyerId == null) {
                    throw new IllegalArgumentException("RESERVED 전환에는 buyerId가 필요합니다.");
                }

                // (정책) 동일 게시글의 다른 예약은 모두 취소하고, 해당 buyerId만 SCHEDULED로 유지
                for (Appointment a : appointments) {
                    if (a.getBuyerId().equals(buyerId)) {
                        a.setStatus(Enum.AppointmentStatus.SCHEDULED);
                    } else {
                        a.setStatus(Enum.AppointmentStatus.CANCELED);
                    }
                }

                // 해당 buyerId의 예약이 없는 경우 새로 생성 (upsert)
                boolean hasTarget = appointments.stream().anyMatch(a -> a.getBuyerId().equals(buyerId));
                if (!hasTarget) {
                    Appointment created = Appointment.builder()
                            .postType("USED_ITEM")
                            .postId(postId)
                            .sellerId(item.getUserId())
                            .buyerId(buyerId)
                            // TODO: 실제 약속 시간/장소로 교체 (채팅에서 입력받는 값 사용)
                            .appointmentAt(LocalDateTime.now())
                            .location(item.getLocation() != null ? item.getLocation() : "미정")
                            .status(Enum.AppointmentStatus.SCHEDULED)
                            .build();
                    appointmentRepository.save(created);
                } else {
                    appointmentRepository.saveAll(appointments);
                }
                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setTitle("[중고거래] 거래물품이 예약중이예요.");
                notificationRequest.setMessage("내역은 마이페이지에서 확인할 수 있어요.");
                notificationRequest.setUserId(buyerId);

                // 3. NotificationService 호출
                notificationService.sendNotification(notificationRequest);
                break;
            }

            case SOLD: {
                // ✅ 거래완료: 보통 예약 걸린 구매자만 COMPLETED 처리
                if (buyerId != null) {
                    for (Appointment a : appointments) {
                        if (a.getBuyerId().equals(buyerId)) {
                            a.setStatus(Enum.AppointmentStatus.COMPLETED);
                        } else {
                            // (정책) 다른 예약은 취소
                            a.setStatus(Enum.AppointmentStatus.CANCELED);
                        }
                    }
                } else {
                    // buyerId가 없으면 해당 글의 모든 예약을 완료 처리(혹은 취소) — 정책 선택
                    // 여기서는 안전하게 모두 완료로 처리하지 않고 모두 취소로 두는 방법도 있음.
                    for (Appointment a : appointments) {
                        a.setStatus(Enum.AppointmentStatus.COMPLETED);
                    }
                }
                appointmentRepository.saveAll(appointments);
                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setTitle("[중고거래] 거래가 정상적으로 완료됬습니다.");
                notificationRequest.setMessage("내역은 마이페이지에서 확인할 수 있어요.");
                notificationRequest.setUserId(buyerId); // 전체 사용자에게 알림 전송용 플래그

                // 3. NotificationService 호출
                notificationService.sendNotification(notificationRequest);
                break;
            }

            case SELLING: {
                // ✅ 판매중(되돌림): 예약을 모두 취소 상태로 변경
                for (Appointment a : appointments) {
                    a.setStatus(Enum.AppointmentStatus.CANCELED);
                }
                appointmentRepository.saveAll(appointments);
                break;
            }

            case DELETED: {
                // ✅ 삭제: 약속은 취소로 처리 (선택 정책)
                for (Appointment a : appointments) {
                    a.setStatus(Enum.AppointmentStatus.CANCELED);
                }
                appointmentRepository.saveAll(appointments);
                break;
            }

        }
    }
}