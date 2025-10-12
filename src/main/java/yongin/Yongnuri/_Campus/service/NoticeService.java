package yongin.Yongnuri._Campus.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.dto.notice.NoticeResponseDto;
import yongin.Yongnuri._Campus.repository.*;
import jakarta.persistence.EntityNotFoundException;
import yongin.Yongnuri._Campus.dto.notice.NoticeCreateRequestDto;
import yongin.Yongnuri._Campus.dto.notice.NoticeUpdateRequestDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import yongin.Yongnuri._Campus.domain.Notice;
import yongin.Yongnuri._Campus.repository.NoticeRepository;
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
        List<Notice> allItems = noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Notice> items = allItems.stream()
                .filter(item -> item.getStatus() != Enum.NoticeStatus.DELETED)
                .collect(Collectors.toList());

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
        if (item.getStatus() == Enum.NoticeStatus.DELETED) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }
        List<Image> images = imageRepository.findByTypeAndTypeIdOrderBySequenceAsc("NOTICE", postId);
        boolean isBookmarked = bookmarkRepository.existsByUserIdAndPostTypeAndPostId(currentUser.getId(), "NOTICE", postId);
        NoticeResponseDto dto = new NoticeResponseDto(item, images);
        dto.setBookmarked(isBookmarked);
        return dto;
    }
    private User getAdminUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
        if (user.getRole() != Enum.UserRole.ADMIN) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }
        return user;
    }
    // 공지사항 작성
    @Transactional
    public Long createNotice(NoticeCreateRequestDto requestDto, String adminEmail) {
        User admin = getAdminUser(adminEmail);
        Notice newNotice = Notice.builder()
                .author(admin)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .status(Enum.NoticeStatus.valueOf(requestDto.getStatus().toUpperCase()))
                .link(requestDto.getLink())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .isImages(requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty())
                .createdAt(LocalDateTime.now())
                .build();

        Notice savedNotice = noticeRepository.save(newNotice);
        Long newPostId = savedNotice.getId();

        if (savedNotice.getIsImages()) {
            int sequence = 1;
            for (String url : requestDto.getImageUrls()) {
                imageRepository.save(Image.builder().type("NOTICE").typeId(newPostId).imageUrl(url).sequence(sequence++).build());
            }
        }
        return newPostId;
    }


   //  공지사항 수정

    @Transactional
    public Long updateNotice(Long noticeId, NoticeUpdateRequestDto requestDto, String adminEmail) {
        User admin = getUserByEmail(adminEmail);
        if (admin.getRole() != Enum.UserRole.ADMIN) {
            throw new AccessDeniedException("공지사항을 수정할 권한이 없습니다.");
        }
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
        if(requestDto.getTitle() != null) notice.setTitle(requestDto.getTitle());
        if(requestDto.getContent() != null) notice.setContent(requestDto.getContent());
        if(requestDto.getStatus() != null) notice.setStatus(Enum.NoticeStatus.valueOf(requestDto.getStatus().toUpperCase()));
        if(requestDto.getLink() != null) notice.setLink(requestDto.getLink());
        if(requestDto.getStartDate() != null) notice.setStartDate(requestDto.getStartDate());
        if(requestDto.getEndDate() != null) notice.setEndDate(requestDto.getEndDate());

        return notice.getId();
    }


    // 공지사항 삭제

    @Transactional
    public void deleteNotice(Long noticeId, String adminEmail) {
        getAdminUser(adminEmail);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
        bookmarkRepository.deleteAllByPostTypeAndPostId("NOTICE", noticeId);
        notice.setStatus(Enum.NoticeStatus.DELETED);
    }
}