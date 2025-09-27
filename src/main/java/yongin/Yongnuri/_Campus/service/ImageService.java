package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
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
public class ImageService {

    @Value("${file.upload-dir}")
    private String uploadDir;
    public List<String> uploadImages(List<MultipartFile> imageFiles) {
        List<String> generatedUrls = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedName = UUID.randomUUID().toString() + extension;
            String savedPath = uploadDir + savedName;
            try {
                file.transferTo(new File(savedPath));
                generatedUrls.add("/uploads/" + savedName);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장에 실패했습니다.", e);
            }
        }
        return generatedUrls;
    }
}