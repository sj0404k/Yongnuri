package yongin.Yongnuri._Campus.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yongin.Yongnuri._Campus.domain.BookMarks;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.Notice;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkRequestDto;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkResponseDto;
import yongin.Yongnuri._Campus.repository.BookMarksRepository;
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

    private final BookMarksRepository bookmarkRepository;
    private final NoticeRepository noticeRepository; // NoticeRepository만 필요
    private final ImageRepository imageRepository;

    @Transactional
    public boolean addBookmark(Long userId, BookmarkRequestDto requestDto) {
        String postType = requestDto.getPostType();
        Long postId = requestDto.getPostId();

        // 공지사항 게시글만 존재하는지 확인
        if ("NOTICE".equals(postType)) {
            if (!noticeRepository.existsById(postId)) {
                throw new EntityNotFoundException("존재하지 않는 게시글입니다.");
            }
        } else {
            // 다른 타입은 이 브랜치에서 지원하지 않음
            throw new IllegalArgumentException("지원하지 않는 게시판 타입입니다.");
        }

        boolean isAlreadyBookmarked = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(userId, postType, postId);

        if (!isAlreadyBookmarked) {
            BookMarks bookmark = BookMarks.builder()
                    .userId(userId)
                    .postType(postType)
                    .postId(postId)
                    .build();
            bookmarkRepository.save(bookmark);
            return true;
        }
        return false;
    }

    @Transactional
    public void removeBookmark(Long userId, BookmarkRequestDto requestDto) {
        // 이 메서드는 이미 범용적이므로 수정할 필요가 없습니다.
        boolean bookmarkExists = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(
                userId, requestDto.getPostType(), requestDto.getPostId());

        if (bookmarkExists) {
            bookmarkRepository.deleteByUserIdAndPostTypeAndPostId(
                    userId, requestDto.getPostType(), requestDto.getPostId());
        } else {
            throw new EntityNotFoundException("이미 관심 목록에 없거나 존재하지 않는 게시글입니다.");
        }
    }

    public List<BookmarkResponseDto> getMyBookmarks(Long userId, String postType) {
        // "NOTICE" 타입이 아닌 요청은 빈 리스트를 반환
        if (!"NOTICE".equals(postType)) {
            return Collections.emptyList();
        }

        List<BookMarks> bookmarks = bookmarkRepository.findByUserIdAndPostTypeOrderByCreatedAtDesc(userId, postType);
        if (bookmarks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> postIds = bookmarks.stream().map(BookMarks::getPostId).collect(Collectors.toList());

        Map<Long, Notice> itemMap = noticeRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Notice::getId, Function.identity()));
        Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("NOTICE", postIds, 1).stream()
                .collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));

        return bookmarks.stream()
                .map(bookmark -> {
                    Notice item = itemMap.get(bookmark.getPostId());
                    if (item == null) return null;
                    String thumbnailUrl = thumbnailMap.get(item.getId());
                    return BookmarkResponseDto.from(item, thumbnailUrl, bookmark);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}