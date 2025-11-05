package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<String> uploadImages(List<MultipartFile> imageFiles) {
        log.info("Uploading images...");
        List<String> generatedUrls = new ArrayList<>();

        // ✅ 업로드 루트 디렉토리 보장
        File root = new File(uploadDir);
        if (!root.exists()) {
            boolean ok = root.mkdirs();
            if (!ok) {
                throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다: " + uploadDir);
            }
        }

        for (MultipartFile file : imageFiles) {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }
            if (".jfif".equals(extension)) {
                extension = ".jpg";
            }
            String savedName = UUID.randomUUID().toString() + extension;
            String savedPath = uploadDir + savedName;

            try {
                file.transferTo(new File(savedPath));
            } catch (IOException e) {
                throw new RuntimeException("파일 저장에 실패했습니다. path=" + savedPath, e);
            }
            generatedUrls.add("/uploads/" + savedName);
        }
        return generatedUrls;
    }
}