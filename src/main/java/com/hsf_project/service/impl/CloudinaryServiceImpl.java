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
        if (file == null|| file.isEmpty()){
            throw new IllegalArgumentException("file must be required");
        }

        Map options = ObjectUtils.asMap("folder", folderName);
        try {
            // Thực hiện gọi API upload
            java.util.Map<?, ?> uploadResult = this.cloudinary.uploader().upload(file.getBytes(), options);

            // Kiểm tra an toàn trước khi lấy dữ liệu tránh lỗi NullPointerException
            if (uploadResult != null && uploadResult.containsKey("secure_url") && uploadResult.get("secure_url") != null) {
                return uploadResult.get("secure_url").toString();
            }

            // In ra console log để chẩn đoán nếu cấu trúc Map trả về bị thiếu
            System.out.println("Cloudinary returned a response but secure_url is missing: " + uploadResult);
            throw new RuntimeException("Secure URL not found in Cloudinary response");

        } catch (Exception e) {
            // In chi tiết lỗi hệ thống kết nối từ Cloudinary (như sai thông tin xác thực)
            System.err.println("Cloudinary upload failed: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }
}
