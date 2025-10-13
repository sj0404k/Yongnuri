package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.dto.notice.NoticeResponseDto;
import yongin.Yongnuri._Campus.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import yongin.Yongnuri._Campus.domain.Notice;
import yongin.Yongnuri._Campus.repository.NoticeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }
    public List<NoticeResponseDto> getNotices(String email) {
        User currentUser = getUserByEmail(email);
        List<Notice> items = noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        if (items.isEmpty()) return List.of();

        List<Long> postIds = items.stream().map(Notice::getId).collect(Collectors.toList());

        Map<Long, String> thumbnailMap = imageRepository.findByTypeAndTypeIdInAndSequence("NOTICE", postIds, 1)
                .stream().collect(Collectors.toMap(Image::getTypeId, Image::getImageUrl));

        Set<Long> myBookmarkedPostIds = bookmarkRepository.findByUserIdAndPostTypeAndPostIdIn(currentUser.getId(), "NOTICE", postIds)
                .stream().map(Bookmark::getPostId).collect(Collectors.toSet());

        return items.stream().map(item -> {
            NoticeResponseDto dto = new NoticeResponseDto(item);
            dto.setThumbnailUrl(thumbnailMap.get(item.getId()));
            dto.setBookmarked(myBookmarkedPostIds.contains(item.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    public NoticeResponseDto getNoticeDetail(String email, Long postId) {
        User currentUser = getUserByEmail(email);
        Notice item = noticeRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("404: 게시글 없음"));
        List<Image> images = imageRepository.findByTypeAndTypeIdOrderBySequenceAsc("NOTICE", postId);
        boolean isBookmarked = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(currentUser.getId(), "NOTICE", postId);
        NoticeResponseDto dto = new NoticeResponseDto(item, images);
        dto.setBookmarked(isBookmarked);
        return dto;
    }
}