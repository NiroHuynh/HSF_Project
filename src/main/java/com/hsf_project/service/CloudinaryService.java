package com.hsf_project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folderName) throws IOException;

    String uploadImageBytes(byte[] bytes, String folderName, String publicId) throws IOException;

    void deleteImage(String publicId) throws IOException;

    String getOptimizedUrl(String url);

    String getResizedUrl(String url, int width, int height);
}
