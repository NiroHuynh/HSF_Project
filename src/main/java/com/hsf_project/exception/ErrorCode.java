package com.hsf_project.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    MOVIE_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "Phim không tồn tại"),
    INVALID_MOVIE_STATUS(1002, HttpStatus.BAD_REQUEST, "Trạng thái phim không hợp lệ"),
    REVIEW_NOT_FOUND(1003, HttpStatus.NOT_FOUND, "Đánh giá không tồn tại"),
    USER_NOT_FOUND(1004, HttpStatus.NOT_FOUND, "Người dùng không tồn tại"),
    REVIEW_ALREADY_EXISTS(1005, HttpStatus.BAD_REQUEST, "Bạn đã đánh giá phim này rồi"),
    INTERNAL_ERROR(9999, HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() { return code; }
    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getMessage() { return message; }
}
