package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.dto.groupbuy.GroupBuyResponseDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemResponseDto;
import yongin.Yongnuri._Campus.repository.*;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemResponseDto;
import yongin.Yongnuri._Campus.repository.*;
import yongin.Yongnuri._Campus.domain.Enum;
import jakarta.persistence.EntityNotFoundException;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkCountDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final UserRepository userRepository;
    private final UsedItemRepository usedItemRepository;
    private final AppointmentRepository appointmentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ImageRepository imageRepository;
    private final LostItemRepository lostItemRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyApplicantRepository applicantRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }
//중고거래내역
    public List<UsedItemResponseDto> getUsedItemTransactions(String email, String type) {
        User currentUser = getUserByEmail(email);

        if ("sell".equalsIgnoreCase(type)) {
            List<Enum.UsedItemStatus> statuses = List.of(Enum.UsedItemStatus.SELLING, Enum.UsedItemStatus.RESERVED, Enum.UsedItemStatus.SOLD);
            List<UsedItem> items = usedItemRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(currentUser.getId(), statuses);
            return convertToDtoList(items, currentUser);

        } else if ("buy".equalsIgnoreCase(type)) {
            List<Appointment> appointments = appointmentRepository.findByBuyerIdAndPostType(currentUser.getId(), "USED_ITEM");
            List<Long> postIds = appointments.stream().map(Appointment::getPostId).collect(Collectors.toList());

            List<Enum.UsedItemStatus> statuses = List.of(Enum.UsedItemStatus.RESERVED, Enum.UsedItemStatus.SOLD);
            List<UsedItem> items = usedItemRepository.findByIdInAndStatusInOrderByCreatedAtDesc(postIds, statuses);
            return convertToDtoList(items, currentUser);
        } else {
            throw new IllegalArgumentException("잘못된 타입입니다. 'sell' 또는 'buy'를 사용하세요.");
        }
    }

    private List<UsedItemResponseDto> convertToDtoList(List<UsedItem> items, User currentUser) {
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = items.stream().map(UsedItem::getId).collect(Collectors.toList());

        Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("USED_ITEM", postIds, 1)
                .stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));

        Set<Long> myBookmarkedPostIds = bookmarkRepository.findByUserIdAndPostTypeAndPostIdIn(currentUser.getId(), "USED_ITEM", postIds)
                .stream().map(Bookmark::getPostId).collect(Collectors.toSet());
        List<BookmarkCountDto> bookmarkCounts = bookmarkRepository.countByPostTypeAndPostIdIn("USED_ITEM", postIds);
        Map<Long, Long> bookmarkCountMap = bookmarkCounts.stream()
                .collect(Collectors.toMap(BookmarkCountDto::getPostId, BookmarkCountDto::getCount));

        return items.stream().map(item -> {
            UsedItemResponseDto dto = new UsedItemResponseDto(item);
            dto.setThumbnailUrl(thumbnailMap.get(item.getId()));
            dto.setBookmarked(myBookmarkedPostIds.contains(item.getId()));
            dto.setBookmarkCount(bookmarkCountMap.getOrDefault(item.getId(), 0L));
            return dto;
        }).collect(Collectors.toList());
    }
   //분실물내역조회
    public List<LostItemResponseDto> getLostItemHistory(String email, String filter) {
        User currentUser = getUserByEmail(email);
        List<LostItem> items;

        switch (filter.toUpperCase()) {
            case "FOUND":
                items = lostItemRepository.findByUserAndPurposeOrderByCreatedAtDesc(currentUser, Enum.LostItemPurpose.FOUND);
                break;
            case "LOST":
                items = lostItemRepository.findByUserAndPurposeOrderByCreatedAtDesc(currentUser, Enum.LostItemPurpose.LOST);
                break;
            case "RETURNED":
                items = lostItemRepository.findByUserAndStatusOrderByCreatedAtDesc(currentUser, Enum.LostItemStatus.RETURNED);
                break;
            default:
                throw new IllegalArgumentException("잘못된 필터입니다. 'found', 'lost', 'returned'를 사용하세요.");
        }
        items = items.stream()
                .filter(item -> item.getStatus() != Enum.LostItemStatus.DELETED)
                .collect(Collectors.toList());

        return convertToLostItemDtoList(items, currentUser);
    }

    private List<LostItemResponseDto> convertToLostItemDtoList(List<LostItem> items, User currentUser) {
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = items.stream().map(LostItem::getId).collect(Collectors.toList());
        Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("LOST_ITEM", postIds, 1)
                .stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));
        Set<Long> myBookmarkedPostIds = bookmarkRepository.findByUserIdAndPostTypeAndPostIdIn(currentUser.getId(), "LOST_ITEM", postIds)
                .stream().map(Bookmark::getPostId).collect(Collectors.toSet());

        return items.stream().map(item -> {
            LostItemResponseDto dto = new LostItemResponseDto(item);
            dto.setThumbnailUrl(thumbnailMap.get(item.getId()));
            dto.setBookmarked(myBookmarkedPostIds.contains(item.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

 //공동구매내역조회
    public List<GroupBuyResponseDto> getGroupBuyHistory(String email, String filter) {
        User currentUser = getUserByEmail(email);
        List<GroupBuy> items;

        if ("registered".equalsIgnoreCase(filter)) {
            items = groupBuyRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        } else if ("applied".equalsIgnoreCase(filter)) {

            List<Long> appliedPostIds = applicantRepository.findByUserId(currentUser.getId())
                    .stream()
                    .map(GroupBuyApplicant::getPostId)
                    .collect(Collectors.toList());


            items = groupBuyRepository.findAllById(appliedPostIds);

        } else {
            throw new IllegalArgumentException("잘못된 필터입니다. 'registered' 또는 'applied'를 사용하세요.");
        }
        items = items.stream()
                .filter(item -> item.getStatus() != Enum.GroupBuyStatus.DELETED)
                .collect(Collectors.toList());
        return convertToGroupBuyDtoList(items, currentUser);


    }
    private List<GroupBuyResponseDto> convertToGroupBuyDtoList(List<GroupBuy> items, User currentUser) {
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = items.stream().map(GroupBuy::getId).collect(Collectors.toList());

        Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("GROUP_BUY", postIds, 1)
                .stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));

        Set<Long> myBookmarkedPostIds = bookmarkRepository.findByUserIdAndPostTypeAndPostIdIn(currentUser.getId(), "GROUP_BUY", postIds)
                .stream().map(Bookmark::getPostId).collect(Collectors.toSet());

        return items.stream().map(item -> {
            GroupBuyResponseDto dto = new GroupBuyResponseDto(item);
            dto.setThumbnailUrl(thumbnailMap.get(item.getId()));
            dto.setBookmarked(myBookmarkedPostIds.contains(item.getId()));
            return dto;
        }).collect(Collectors.toList());
    }
}
