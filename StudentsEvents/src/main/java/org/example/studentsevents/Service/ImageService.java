

// (Full and final version for Priority 1)

package org.example.studentsevents.Service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    private final Path fileStorageLocation;

    // --- Validation Constants ---
    // Maximum file size set to 5 Megabytes.
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    // The official, IANA-registered MIME types for allowed images.
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", // .jpg and .jpeg
            "image/png",  // .png
            "image/webp", // .webp
            "image/avif"  // .avif
    );

    public ImageService() {
        // This will create an "uploads" folder in the root directory of your project
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // --- File Validation Block ---
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
        // --- End of Validation ---


        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch (Exception e) {
            // No extension found, which is acceptable.
        }

        // Generate a unique filename to prevent conflicts
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + newFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }
}