package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import yongin.Yongnuri._Campus.config.TimeUtils;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.dto.SearchBoard;
import yongin.Yongnuri._Campus.dto.SearchReq;
import yongin.Yongnuri._Campus.dto.SearchRes;
import yongin.Yongnuri._Campus.repository.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
    private final LostItemRepository lostItemRepository;
    private final UsedItemRepository usedItemRepository;
    private final NoticeRepository noticeRepository;
    private final GroupBuyRepository groupbuyRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ImageRepository imageRepository;
    public boolean deleteAllHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        searchRepository.deleteByUserId(user.getId());

        return true;
    }

    public boolean deleteHistoryById(String email, Long searchId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        Optional<Search> searchOpt = searchRepository.findById(searchId);

        if (searchOpt.isPresent()) {
            searchRepository.delete(searchOpt.get());
            return true; // 삭제 성공
        }
        return false; // 삭제 실패 (존재하지 않음)
    }

    public List<SearchRes> getHistory(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        List<Search> searches = searchRepository.findAllByUserId(currentUser.getId());

        return searches.stream()
                .map(search -> SearchRes.builder()
                        .searchId(search.getId())
                        .query(search.getQuery())
                        .build()
                )
                .toList();
    }

    public List<SearchBoard> getBoard(String email, SearchReq.SearchDto searchReq) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        Search search =Search.builder()
                .userId(currentUser.getId())
                .query(searchReq.getQuery())
                .date(LocalDateTime.now())
                .build();
        searchRepository.save(search);

        // 1차: 키워드 포함 검색
        List<LostItem> lostItems = lostItemRepository.findByTitleContainingIgnoreCase(searchReq.getQuery());
        List<UsedItem> usedItems = usedItemRepository.findByTitleContainingIgnoreCase(searchReq.getQuery());
        List<Notice> notices = noticeRepository.findByTitleContainingIgnoreCase(searchReq.getQuery());
        List<GroupBuy> groupBuys = groupbuyRepository.findByTitleContainingIgnoreCase(searchReq.getQuery());

        List<Long> lostItemIds = lostItems.stream().map(LostItem::getId).collect(Collectors.toList());
        List<Long> usedItemIds = usedItems.stream().map(UsedItem::getId).collect(Collectors.toList());
        List<Long> noticeIds = notices.stream().map(Notice::getId).collect(Collectors.toList());
        List<Long> groupBuyIds = groupBuys.stream().map(GroupBuy::getId).collect(Collectors.toList());

        Map<Long, String> usedItemThumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("USED_ITEM", usedItemIds, 1)
                .stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));
        Map<Long, String> lostItemThumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("LOST_ITEM", lostItemIds, 1)
                .stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));
        Map<Long, String> noticeThumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("NOTICE", noticeIds, 1)
                .stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));
        Map<Long, String> groupBuyThumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("GROUP_BUY", groupBuyIds, 1)
                .stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));

        List<SearchBoard> lostResults = lostItems.stream()
                .map(item -> {
                    Integer likeCount = bookmarkRepository.countByPostTypeAndPostId("LOST_ITEM", item.getId());
                    String thumbnailUrl = lostItemThumbnailMap.get(item.getId());

                    return SearchBoard.builder()
                            .id(item.getId())
                            .title(item.getTitle())
                            .location(item.getLocation())
                            .price(null)
                            .createdAt(TimeUtils.toRelativeTime(item.getCreatedAt()))
                            .boardType("분실물")
                            .like(likeCount)
                            .thumbnailUrl(thumbnailUrl)
                            .build();
                })
                .toList();

        List<SearchBoard> usedResults = usedItems.stream()
                .map(item -> {
                    Integer likeCount = bookmarkRepository.countByPostTypeAndPostId("USED_ITEM", item.getId());
                    String thumbnailUrl = usedItemThumbnailMap.get(item.getId());
                    return SearchBoard.builder()
                            .id(item.getId())
                            .title(item.getTitle())
                            .location(item.getLocation())
                            .price(item.getPrice())
                            .createdAt(TimeUtils.toRelativeTime(item.getCreatedAt())).boardType("중고거래")
                            .like(likeCount)
                            .thumbnailUrl(thumbnailUrl)
                            .build();
                })
                .toList();
        List<SearchBoard> groupResults = groupBuys.stream()
                .map(item -> {
                    Integer likeCount = bookmarkRepository.countByPostTypeAndPostId("GROUP_BUY", item.getId());
                    String thumbnailUrl = groupBuyThumbnailMap.get(item.getId());
                    return SearchBoard.builder()
                            .id(item.getId())
                            .title(item.getTitle())
                            .location(null)
                            .price(null)
                            .createdAt(TimeUtils.toRelativeTime(item.getCreatedAt()))
                            .boardType("공동구매")
                            .like(likeCount)
                            .thumbnailUrl(thumbnailUrl)
                            .build();
                })
                .toList();

        List<SearchBoard> noticeResults = notices.stream()
                .map(item -> {
                    Integer likeCount = bookmarkRepository.countByPostTypeAndPostId("NOTICE", item.getId());
                    String thumbnailUrl = noticeThumbnailMap.get(item.getId());
                    return SearchBoard.builder()
                            .id(item.getId())
                            .title(item.getTitle())
                            .location(null)
                            .price(null)
                            .createdAt(TimeUtils.toRelativeTime(item.getCreatedAt()))
                            .boardType("공지홍보")
                            .like(likeCount)
                            .thumbnailUrl(thumbnailUrl)
                            .build();
                })
                .toList();

        return Stream.of(lostResults, usedResults, noticeResults, groupResults)
                .flatMap(List::stream) // 각 리스트를 스트림으로 변환하여 하나의 스트림으로 연결
                .sorted(Comparator.comparing(SearchBoard::getCreatedAt).reversed())
                .toList();


    }



    public void addHistory(String email, SearchReq searchReq) {

    }
}

