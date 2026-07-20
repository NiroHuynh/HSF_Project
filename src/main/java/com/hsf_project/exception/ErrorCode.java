package com.hsf_project.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    MOVIE_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "Phim không tồn tại"),
    INVALID_MOVIE_STATUS(1002, HttpStatus.BAD_REQUEST, "Trạng thái phim không hợp lệ"),
    REVIEW_NOT_FOUND(1003, HttpStatus.NOT_FOUND, "Đánh giá không tồn tại"),
    USER_NOT_FOUND(1004, HttpStatus.NOT_FOUND, "Người dùng không tồn tại"),
    REVIEW_ALREADY_EXISTS(1005, HttpStatus.BAD_REQUEST, "Bạn đã đánh giá phim này rồi"),
    MOVIE_TITLE_EXISTS(1006, HttpStatus.CONFLICT, "Tên phim đã tồn tại"),
    MOVIE_HAS_SHOWTIMES(1007, HttpStatus.CONFLICT, "Phim đã có suất chiếu, không thể xóa"),
    MOVIE_HAS_BOOKINGS(1008, HttpStatus.CONFLICT, "Phim đã có vé đặt, không thể xóa"),
    MOVIE_NOW_SHOWING(1009, HttpStatus.BAD_REQUEST, "Phim đang chiếu, không thể cập nhật trường này"),
    MOVIE_ENDED(1010, HttpStatus.BAD_REQUEST, "Phim đã kết thúc"),
    INVALID_RELEASE_DATE(1011, HttpStatus.BAD_REQUEST, "Ngày khởi chiếu không được trong quá khứ"),
    INVALID_GENRE(1012, HttpStatus.BAD_REQUEST, "Thể loại không tồn tại"),
    INVALID_STATUS_TRANSITION(1013, HttpStatus.BAD_REQUEST, "Chuyển trạng thái không hợp lệ"),
    FILE_TOO_LARGE(1014, HttpStatus.BAD_REQUEST, "File vượt quá kích thước cho phép (5MB)"),
    INVALID_FILE_FORMAT(1015, HttpStatus.BAD_REQUEST, "Định dạng file không hợp lệ. Chỉ chấp nhận jpg, jpeg, png, webp"),
    FORBIDDEN(1016, HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện hành động này"),
    UNAUTHORIZED(1017, HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập"),
    MOVIE_CANNOT_CANCEL(1018, HttpStatus.BAD_REQUEST, "Chỉ có thể hủy phim đang chiếu hoặc sắp chiếu"),
    NO_FUTURE_SHOWTIMES(1019, HttpStatus.BAD_REQUEST, "Phim không có suất chiếu trong tương lai"),
    CUSTOMER_NOT_FOUND(1020, HttpStatus.NOT_FOUND, "Khách hàng không tồn tại"),
    INVALID_DATE_RANGE(1021, HttpStatus.BAD_REQUEST, "Khoảng thời gian không hợp lệ"),
    INVALID_GROWTH_TYPE(1022, HttpStatus.BAD_REQUEST, "Loại thống kê không hợp lệ, chỉ chấp nhận month/quarter/year"),
    INVALID_SORT_FIELD(1023, HttpStatus.BAD_REQUEST, "Trường sắp xếp không hợp lệ"),
    NO_BOOKING_DATA(1024, HttpStatus.NOT_FOUND, "Không có dữ liệu đặt vé"),
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
