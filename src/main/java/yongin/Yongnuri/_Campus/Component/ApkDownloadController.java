package yongin.Yongnuri._Campus.Component;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;

@RestController
public class ApkDownloadController {

    @GetMapping("/Yongnuri.apk")
    public ResponseEntity<Resource> downloadApk() {
        File file = new File("src/main/resources/static/Yongnuri.apk");
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Yongnuri.apk")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
