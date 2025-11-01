package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.repository.ChatMessagesRepository;
import yongin.Yongnuri._Campus.repository.ImageRepository;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageCleanupService {

    private final ImageRepository imageRepository;
    private final ChatMessagesRepository chatMessagesRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

   //이미지 등록만하고 글 등록하지 않는 등의 문제로 db에 없는 이미지 파일 폴더에서 삭제
   // 게시글 이미지, 채팅 이미지 메시지 확인
    @Scheduled(cron = "0 0 17 * * ?")
    public void cleanupUnusedImages() {
        System.out.println("매일 오후 5시 - 불필요한 이미지 파일 정리를 시작합니다...");
        File uploadFolder = new File(uploadDir);
        File[] imageFiles = uploadFolder.listFiles();
        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("정리할 이미지 파일이 없습니다.");
            return;
        }
        Set<String> dbImageUrls = imageRepository.findAll().stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toSet());
        List<String> chatImageUrls = chatMessagesRepository.findAllImageUrls();
        dbImageUrls.addAll(chatImageUrls);
        int deleteCount = 0;
        for (File file : imageFiles) {
            String fileUrl = "/uploads/" + file.getName();
            if (!dbImageUrls.contains(fileUrl)) {
                if (file.delete()) {
                    System.out.println("삭제된 파일: " + file.getName());
                    deleteCount++;
                }
            }
        }
        System.out.println("총 " + deleteCount + "개의 불필요한 이미지 파일을 정리했습니다.");
    }
}