package com.hsf_project.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hsf_project.service.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(MultipartFile file, String folderName) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file must be required");
        }

        Map options = ObjectUtils.asMap("folder", folderName);
        try {
            java.util.Map<?, ?> uploadResult = this.cloudinary.uploader().upload(file.getBytes(), options);

            if (uploadResult != null && uploadResult.containsKey("secure_url") && uploadResult.get("secure_url") != null) {
                return uploadResult.get("secure_url").toString();
            }

            System.out.println("Cloudinary returned a response but secure_url is missing: " + uploadResult);
            throw new RuntimeException("Secure URL not found in Cloudinary response");

        } catch (Exception e) {
            System.err.println("Cloudinary upload failed: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadImageBytes(byte[] bytes, String folderName, String publicId) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("bytes must be required");
        }

        Map options = ObjectUtils.asMap("folder", folderName, "public_id", publicId);
        try {
            java.util.Map<?, ?> uploadResult = this.cloudinary.uploader().upload(bytes, options);

            if (uploadResult != null && uploadResult.containsKey("secure_url") && uploadResult.get("secure_url") != null) {
                return uploadResult.get("secure_url").toString();
            }

            throw new RuntimeException("Secure URL not found in Cloudinary response");
        } catch (Exception e) {
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String publicId) throws IOException {
        try {
            java.util.Map<?, ?> result = this.cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            if (result != null && !"ok".equals(result.get("result"))) {
                throw new IOException("Cloudinary delete failed: " + result);
            }
        } catch (Exception e) {
            System.err.println("Cloudinary delete failed: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public String getOptimizedUrl(String url) {
        if (url == null || !url.contains("res.cloudinary.com")) {
            return url;
        }
        return url.replace("/image/upload/", "/image/upload/f_auto,q_auto/");
    }

    @Override
    public String getResizedUrl(String url, int width, int height) {
        if (url == null || !url.contains("res.cloudinary.com")) {
            return url;
        }
        String transform = "c_fill,w_" + width + ",h_" + height + ",f_auto,q_auto";
        return url.replace("/image/upload/", "/image/upload/" + transform + "/");
    }
}
