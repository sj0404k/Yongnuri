package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.repository.*;
import jakarta.persistence.EntityNotFoundException;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final UserRepository userRepository;
    private final UsedItemRepository usedItemRepository;
    private final LostItemRepository lostItemRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;
    private final GroupBuyApplicantRepository applicantRepository;
    private final AppointmentRepository appointmentRepository;
    @Value("${file.upload-dir}") // application.properties의 파일 저장 경로
    private String uploadDir;
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void deletePost(String email, String postType, Long postId) {
        User currentUser = getUserByEmail(email);
        Long authorId;
        Object itemToUpdate = null;

        switch (postType) {
            case "USED_ITEM":
                UsedItem usedItem = usedItemRepository.findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 중고거래 게시글입니다."));
                authorId = usedItem.getUserId();
                itemToUpdate = usedItem;
                break;
            case "LOST_ITEM":
                LostItem lostItem = lostItemRepository.findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 분실물 게시글입니다."));
                authorId = lostItem.getUser().getId();
                itemToUpdate = lostItem;
                break;
            case "GROUP_BUY":
                GroupBuy groupBuy = groupBuyRepository.findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공동구매 게시글입니다."));
                authorId = groupBuy.getUserId();
                itemToUpdate = groupBuy;
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 게시판 타입입니다.");
        }


        boolean isAuthor = authorId.equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Enum.UserRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("이 게시글을 삭제할 권한이 없습니다.");
        }
        List<Image> imagesToDelete = imageRepository.findByTypeAndTypeIdOrderBySequenceAsc(postType, postId);

     //게시글삭제시이미지삭제
        for (Image image : imagesToDelete) {
            String fileName = image.getImageUrl().substring("/uploads/".length());
            File file = new File(uploadDir + fileName);
            if (file.exists()) {
                file.delete();
            }
        }
// 게시글 삭제시 관심목록 삭제
        bookmarkRepository.deleteAllByPostTypeAndPostId(postType, postId);
        imageRepository.deleteAll(imagesToDelete);
        if (itemToUpdate instanceof UsedItem) {
            ((UsedItem) itemToUpdate).setStatus(Enum.UsedItemStatus.DELETED);
        } else if (itemToUpdate instanceof LostItem) {
            ((LostItem) itemToUpdate).setStatus(Enum.LostItemStatus.DELETED);
        } else if (itemToUpdate instanceof GroupBuy) {
            ((GroupBuy) itemToUpdate).setStatus(Enum.GroupBuyStatus.DELETED);
        }
    }
}