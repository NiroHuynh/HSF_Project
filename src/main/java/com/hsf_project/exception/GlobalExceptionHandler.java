package com.hsf_project.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public String handleAppException(AppException ex, Model model) {
        log.warn("AppException: {}", ex.getMessage());
        model.addAttribute("errorCode", ex.getErrorCode().getCode());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        model.addAttribute("errorCode", 400);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        // Che chi tiết lỗi với user nhưng phải log đầy đủ stacktrace để debug
        log.error("Lỗi hệ thống chưa được xử lý", ex);
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống");
        return "error";
    }
}
