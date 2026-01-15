// FILE: D:\students-fullstack\StudentsEvents\src\main\java\org\example\studentsevents\Service\ImageService.java

package org.example.studentsevents.Service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/avif"
    );

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("Cannot store an empty file.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalStateException("File size exceeds the limit of 5 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalStateException("Invalid file type. Only JPG, PNG, WEBP, and AVIF images are permitted.");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of());
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Could not store file with Cloudinary. Please try again!", e);
        }
    }

}