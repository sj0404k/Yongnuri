package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import yongin.Yongnuri._Campus.service.ImageService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImages(
            @RequestParam("images") List<MultipartFile> imageFiles
    ) {
        List<String> imageUrls = imageService.uploadImages(imageFiles);
        return ResponseEntity.ok(Map.of("imageUrls", imageUrls));
    }
}