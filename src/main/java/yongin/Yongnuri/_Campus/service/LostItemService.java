package yongin.Yongnuri._Campus.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.dto.NotificationRequest;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkCountDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemCreateRequestDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemResponseDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemUpdateRequestDto;
import yongin.Yongnuri._Campus.dto.chat.SystemMessageDto;
import yongin.Yongnuri._Campus.repository.*;

@Service
@RequiredArgsConstructor
public class LostItemService {
    private static final Logger log = LoggerFactory.getLogger(LostItemService.class);

    private final LostItemRepository lostItemRepository;
    private final BlockService blockService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    // ✅ 추가: 채팅방/웹소켓
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }

    /* 분실물 게시글 목록 조회 */
    public List<LostItemResponseDto> getLostItems(String email, String type) {
        User currentUser = getUserByEmail(email);
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUser.getId());

        Specification<LostItem> spec = (root, query, cb) ->
                cb.not(root.get("user").get("id").in(blockedUserIds));
        spec = spec.and((root, query, cb) -> cb.notEqual(root.get("status"), Enum.LostItemStatus.DELETED));
        if (!"전체".equals(type)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("location"), type));
        }

        List<LostItem> items = lostItemRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"));
        if (items.isEmpty()) return List.of();

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

        List<BookmarkCountDto> bookmarkCounts =
                bookmarkRepository.findBookmarkCountsByPostTypeAndPostIdIn("LOST_ITEM", postIds);
        Map<Long, Long> bookmarkCountMap = bookmarkCounts.stream()
                .collect(Collectors.toMap(BookmarkCountDto::getPostId, BookmarkCountDto::getCount));

        return items.stream()
                .map(item -> {
                    LostItemResponseDto dto = new LostItemResponseDto(item);
                    dto.setThumbnailUrl(thumbnailMap.get(item.getId()));
                    dto.setBookmarked(myBookmarkedPostIds.contains(item.getId()));
                    dto.setBookmarkCount(bookmarkCountMap.getOrDefault(item.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /* 상세조회 */
    public LostItemResponseDto getLostItemDetail(String email, Long postId) {
        User currentUser = getUserByEmail(email);
        List<Long> blockedUserIds = blockService.getBlockedUserIds(currentUser.getId());

        LostItem item = lostItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));
        if (item.getStatus() == Enum.LostItemStatus.DELETED) {
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
        long bookmarkCount = bookmarkRepository.countByPostTypeAndPostId("LOST_ITEM", postId);
        dto.setBookmarkCount(bookmarkCount);
        boolean isBookmarked = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(
                currentUser.getId(), "LOST_ITEM", postId);
        dto.setBookmarked(isBookmarked);

        return dto;
    }

    /* 작성 */
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

    /* 수정 */
    @Transactional
    public Long updateLostItem(String email, Long postId, LostItemUpdateRequestDto requestDto) {
        User currentUser = getUserByEmail(email);
        LostItem item = lostItemRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));

        if (!item.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("403: 이 게시글을 수정할 권한이 없습니다.");
        }

        // 이미지 교체
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

        if (requestDto.getTitle() != null)    item.setTitle(requestDto.getTitle());
        if (requestDto.getContent() != null)  item.setContent(requestDto.getContent());
        if (requestDto.getLocation() != null) item.setLocation(requestDto.getLocation());
        if (requestDto.getPurpose() != null)
            item.setPurpose(Enum.LostItemPurpose.valueOf(requestDto.getPurpose().toUpperCase()));

        // ✅ 상태 변경 처리
        if (requestDto.getStatus() != null) {
            Enum.LostItemStatus newStatus = Enum.LostItemStatus.valueOf(requestDto.getStatus().toUpperCase());
            Enum.LostItemStatus oldStatus = item.getStatus();

            if (newStatus != oldStatus) {
                // 약속 불러오기
                List<Appointment> appointments =
                        appointmentRepository.findByPostTypeAndPostId("LOST_ITEM", postId);

                switch (newStatus) {
                    case RETURNED: {
                        // 1) 약속 완료 처리
                        for (Appointment a : appointments) {
                            a.setStatus(Enum.AppointmentStatus.COMPLETED);
                        }
                        if (!appointments.isEmpty()) {
                            appointmentRepository.saveAll(appointments);
                        }

                        // 2) 이 게시글과 연결된 모든 채팅방 참가자 수집 (작성자 본인은 제외)
                        List<ChatRoom> rooms =
                                chatRoomRepository.findByTypeAndTypeIdWithParticipantsAndLock(Enum.ChatType.LOST_ITEM, postId);

                        Set<Long> targetUserIds = rooms.stream()
                                .flatMap(r -> r.getParticipants().stream()) // 참가자 엔티티 타입명과 무관
                                .map(p -> p.getUser())
                                .filter(u -> u != null)
                                .map(User::getId)
                                .filter(uid -> uid != null && !uid.equals(currentUser.getId()))
                                .collect(Collectors.toSet());

                        // 3) FCM/알림 발송
                        if (!targetUserIds.isEmpty()) {
                            NotificationRequest req = new NotificationRequest();
                            req.setTitle("[분실물] 물품이 회수(반환)되었습니다.");
                            req.setMessage(String.format(
                                    "'%s' 건(%s, %s)의 물품이 처리되었습니다. 마이페이지에서 확인하세요.",
                                    item.getTitle(),
                                    item.getPurpose().name(),
                                    item.getLocation() != null ? item.getLocation() : "장소 미정"
                            ));
                            req.setTargetUserIds(new ArrayList<>(targetUserIds));
                            notificationService.sendNotification(req);
                        }

                        // 4) 각 채팅방에 시스템 메시지 브로드캐스트
                        SystemMessageDto sys = SystemMessageDto.of(
                                "분실물이 회수 처리되었습니다. 마이페이지 > 거래내역 > 분실물 탭에서 확인하세요."
                        );
                        for (ChatRoom room : rooms) {
                            messagingTemplate.convertAndSend("/sub/chat/" + room.getId(), sys);
                        }
                        break;
                    }

                    case DELETED:
                    case REPORTED: {
                        // 삭제/신고 시: 약속 취소
                        for (Appointment a : appointments) {
                            a.setStatus(Enum.AppointmentStatus.CANCELED);
                        }
                        if (!appointments.isEmpty()) {
                            appointmentRepository.saveAll(appointments);
                        }
                        break;
                    }
                }

                item.setStatus(newStatus);
            }
        }
        return item.getId();
    }
}
