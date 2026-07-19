package com.hsf_project.exception;

import com.hsf_project.dto.common.ApiResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * Xử lý lỗi cho các endpoint trả JSON.
 *
 * Phải giới hạn vào @RestController và đặt độ ưu tiên cao nhất: nếu để mặc định thì
 * thứ tự giữa advice này và GlobalExceptionHandler (@ControllerAdvice) là không xác
 * định, nên một exception ném ra từ endpoint JSON có thể bị advice MVC bắt và trả về
 * trang HTML "error" kèm status 500. Phía trình duyệt res.json() parse HTML thất bại,
 * rơi vào nhánh .catch() và hiện "Lỗi kết nối mạng" thay vì thông báo thật
 * (ví dụ "Email đã tồn tại trong hệ thống").
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = RestController.class)
public class GlobalRestExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return new ResponseEntity<>(
                ApiResponse.error(ex.getErrorCode().getCode(), ex.getMessage()),
                ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return new ResponseEntity<>(
                ApiResponse.error(400, msg),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable() {
        return new ResponseEntity<>(
                ApiResponse.error(400, "Dữ liệu gửi lên không hợp lệ"),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded() {
        return new ResponseEntity<>(
                ApiResponse.error(1014, "File vượt quá kích thước cho phép (5MB)"),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                ApiResponse.error(400, ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return new ResponseEntity<>(
                ApiResponse.error(9999, "Lỗi hệ thống: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
