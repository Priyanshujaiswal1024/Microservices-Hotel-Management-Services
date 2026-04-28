package com.hotel.hotel.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "hotel-booking",  // Cloudinary folder
                            "resource_type", "image"
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded to Cloudinary: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            // URL se public_id nikalo
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.warn("Image delete failed: {}", e.getMessage());
        }
    }

    private String extractPublicId(String imageUrl) {
        // URL: https://res.cloudinary.com/cloud/image/upload/v123/hotel-booking/abc.jpg
        // Public ID: hotel-booking/abc
        String[] parts = imageUrl.split("/upload/");
        String afterUpload = parts[1];
        // version number remove karo
        if (afterUpload.startsWith("v")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        // extension remove karo
        return afterUpload.substring(0, afterUpload.lastIndexOf("."));
    }
}