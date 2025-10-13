/**package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.repository.*;
import yongin.Yongnuri._Campus.domain.Enum;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CleanupService {

    private final UsedItemRepository usedItemRepository;
    private final LostItemRepository lostItemRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final NoticeRepository noticeRepository;
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AppointmentRepository appointmentRepository;
    private final GroupBuyApplicantRepository applicantRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;


    // 매일 17시에 실행: 90일이 지난 DELETED 상태의 게시글을 완전 삭제

    @Scheduled(cron = "0 0 17 * * ?")
    @Transactional
    public void cleanupDeletedPosts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        System.out.println("90일 지난 삭제된 게시글 정리를 시작합니다...");

        List<UsedItem> usedItemsToDelete = usedItemRepository.findAllByStatusAndCreatedAtBefore(Enum.UsedItemStatus.DELETED, cutoffDate);
        permanentlyDeletePosts("USED_ITEM", usedItemsToDelete.stream().map(UsedItem::getId).collect(Collectors.toList()));

        List<LostItem> lostItemsToDelete = lostItemRepository.findAllByStatusAndCreatedAtBefore(Enum.LostItemStatus.DELETED, cutoffDate);
        permanentlyDeletePosts("LOST_ITEM", lostItemsToDelete.stream().map(LostItem::getId).collect(Collectors.toList()));
        // ... (LostItem, GroupBuy, Notice에 대해서도 동일한 로직 반복)
    }


   //매월 1일 17시에 실행: 5년이 지난 모든 게시글을 영구 삭제합니다.

    @Scheduled(cron = "0 0 17 1 * ?")
    @Transactional
    public void cleanupOldPosts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(5);
        System.out.println("5년 지난 오래된 게시글 정리를 시작합니다...");


        List<UsedItem> usedItemsToDelete = usedItemRepository.findAllByCreatedAtBefore(cutoffDate);
        permanentlyDeletePosts("USED_ITEM", usedItemsToDelete.stream().map(UsedItem::getId).collect(Collectors.toList()));

    }

    private void permanentlyDeletePosts(String postType, List<Long> postIds) {
        if (postIds.isEmpty()) {
            return;
        }

        List<Image> imagesToDelete = imageRepository.findByTypeAndTypeIdIn(postType, postIds);
        for (Image image : imagesToDelete) {
            String fileName = image.getImageUrl().substring("/uploads/".length());
            File file = new File(uploadDir + fileName);
            if (file.exists()) {
                file.delete();
            }
        }
        imageRepository.deleteAll(imagesToDelete);

        for(Long postId : postIds) {
            bookmarkRepository.deleteAllByPostTypeAndPostId(postType, postId);
            appointmentRepository.deleteAllByPostTypeAndPostId(postType, postId);
            if ("GROUP_BUY".equals(postType)) {
                applicantRepository.deleteAllByPostId(postId);
            }
        }


        switch (postType) {
            case "USED_ITEM":
                usedItemRepository.deleteAllById(postIds);
                break;
            case "LOST_ITEM":
                lostItemRepository.deleteAllById(postIds);
                break;
            case "GROUP_BUY":
                groupBuyRepository.deleteAllById(postIds);
                break;
            case "NOTICE":
                noticeRepository.deleteAllById(postIds);
                break;
        }
        System.out.println(postType + " 게시글 " + postIds.size() + "개 및 관련 데이터 영구 삭제 완료.");
    }
}*/