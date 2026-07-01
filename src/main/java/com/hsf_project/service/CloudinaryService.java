package com.hsf_project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface CloudinaryService {
    public String uploadImage(MultipartFile file, String folderName) throws IOException;

}
