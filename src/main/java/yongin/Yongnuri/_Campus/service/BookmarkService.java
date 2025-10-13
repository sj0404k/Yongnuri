package yongin.Yongnuri._Campus.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.domain.Bookmark;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.Notice;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkCountDto;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkRequestDto;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkResponseDto;
import yongin.Yongnuri._Campus.repository.*;
import jakarta.persistence.EntityNotFoundException;
import yongin.Yongnuri._Campus.repository.BookmarkRepository;
import yongin.Yongnuri._Campus.repository.ImageRepository;
import yongin.Yongnuri._Campus.repository.NoticeRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UsedItemRepository usedItemRepository;
    private final LostItemRepository lostItemRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final NoticeRepository noticeRepository;
    private final ImageRepository imageRepository;
//관심목록추가
    @Transactional
    public boolean addBookmark(Long userId, BookmarkRequestDto requestDto) {
        String postType = requestDto.getPostType();
        Long postId = requestDto.getPostId();

        boolean postExists = false;
        switch (postType) {
            case "USED_ITEM":
                postExists = usedItemRepository.existsById(postId);
                break;
            case "LOST_ITEM":
                postExists = lostItemRepository.existsById(postId);
                break;
            case "GROUP_BUY":
                postExists = groupBuyRepository.existsById(postId);
                break;
            case "NOTICE":
                postExists = noticeRepository.existsById(postId);
                break;

        }
        if (!postExists) {
            throw new EntityNotFoundException("존재하지 않는 게시글입니다.");
        }
        boolean isAlreadyBookmarked = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(userId, postType, postId);

        if (!isAlreadyBookmarked) {
            Bookmark bookmark = Bookmark.builder()
                    .userId(userId)
                    .postType(postType)
                    .postId(postId)
                    .build();
            bookmarkRepository.save(bookmark);
            return true;
        }
        return false;
    }
//관심목록삭제

    @Transactional
    public void removeBookmark(Long userId, BookmarkRequestDto requestDto) {
        boolean bookmarkExists = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(
                userId, requestDto.getPostType(), requestDto.getPostId());

        if (bookmarkExists) {
            bookmarkRepository.deleteByUserIdAndPostTypeAndPostId(
                    userId, requestDto.getPostType(), requestDto.getPostId());
        } else {
            throw new EntityNotFoundException("이미 관심 목록에 없거나 존재하지 않는 게시글입니다.");
        }
    }

  //관심목록조회
    public List<BookmarkResponseDto> getMyBookmarks(Long userId, String postType) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdAndPostTypeOrderByCreatedAtDesc(userId, postType);
        if (bookmarks.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = bookmarks.stream().map(Bookmark::getPostId).collect(Collectors.toList());
        Map<Long, Long> likeCountMap = bookmarkRepository.findBookmarkCountsByPostTypeAndPostIdIn(postType, postIds)
                .stream()
                .collect(Collectors.toMap(BookmarkCountDto::getPostId, BookmarkCountDto::getCount));

        if ("USED_ITEM".equals(postType)) {
            Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("USED_ITEM", postIds, 1).stream()
                    .collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));
            List<UsedItem> items = usedItemRepository.findAllById(postIds).stream()
                    .filter(item -> item.getStatus() != Enum.UsedItemStatus.DELETED)
                    .collect(Collectors.toList());
            Map<Long, UsedItem> itemMap = items.stream().collect(Collectors.toMap(UsedItem::getId, item -> item));
            return bookmarks.stream()
                    .map(bookmark -> {
                        UsedItem item = itemMap.get(bookmark.getPostId());
                        if (item == null) return null;
                        String thumbnailUrl = thumbnailMap.get(item.getId());
                        long likeCount = likeCountMap.getOrDefault(item.getId(), 0L);
                        return BookmarkResponseDto.from(item, thumbnailUrl, bookmark, likeCount);

                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } else if ("LOST_ITEM".equals(postType)) {

            Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("LOST_ITEM", postIds, 1).stream()
                    .collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));
            List<LostItem> items = lostItemRepository.findAllById(postIds).stream()
                    .filter(item -> item.getStatus() != Enum.LostItemStatus.DELETED)
                    .collect(Collectors.toList());
            Map<Long, LostItem> itemMap = items.stream().collect(Collectors.toMap(LostItem::getId, item -> item));
            return bookmarks.stream()
                    .map(bookmark -> {
                        LostItem item = itemMap.get(bookmark.getPostId());
                        if (item == null) return null;
                        String thumbnailUrl = thumbnailMap.get(item.getId());
                        long likeCount = likeCountMap.getOrDefault(item.getId(), 0L);
                        return BookmarkResponseDto.from(item, thumbnailUrl, bookmark, likeCount);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }else if ("GROUP_BUY".equals(postType)) {

            Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("GROUP_BUY", postIds, 1).stream()
                    .collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));
            List<GroupBuy> items = groupBuyRepository.findAllById(postIds).stream()
                    .filter(item -> item.getStatus() != Enum.GroupBuyStatus.DELETED)
                    .collect(Collectors.toList());
            Map<Long, GroupBuy> itemMap = items.stream().collect(Collectors.toMap(GroupBuy::getId, item -> item));
            return bookmarks.stream()
                    .map(bookmark -> {
                        GroupBuy item = itemMap.get(bookmark.getPostId());
                        if (item == null) return null;
                        String thumbnailUrl = thumbnailMap.get(item.getId());
                        long likeCount = likeCountMap.getOrDefault(item.getId(), 0L);
                        return BookmarkResponseDto.from(item, thumbnailUrl, bookmark, likeCount);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }else if ("NOTICE".equals(postType)) {
            List<Notice> items = noticeRepository.findAllById(postIds).stream()
                    .filter(item -> item.getStatus() != Enum.NoticeStatus.DELETED)
                    .collect(Collectors.toList());
            Map<Long, Notice> itemMap = items.stream().collect(Collectors.toMap(Notice::getId, item -> item));
                Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("NOTICE", postIds, 1).stream()
                        .collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));
                return bookmarks.stream()
                        .map(bookmark -> {
                            Notice item = itemMap.get(bookmark.getPostId());
                            if (item == null) return null;
                            String thumbnailUrl = thumbnailMap.get(item.getId());
                            long likeCount = likeCountMap.getOrDefault(item.getId(), 0L);
                            return BookmarkResponseDto.from(item, thumbnailUrl, bookmark, likeCount);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        }

        return List.of();
    }
}