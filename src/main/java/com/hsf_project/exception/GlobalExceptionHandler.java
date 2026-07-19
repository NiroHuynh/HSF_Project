package com.hsf_project.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Dựng trang lỗi kèm ĐÚNG HTTP status. Trước đây handler trả về tên view trần
     * nên mọi trang lỗi đều đi kèm status 200 — trình duyệt, crawler và cả script
     * kiểm thử đều tưởng request thành công.
     */
    private ModelAndView errorView(HttpStatus status, int errorCode, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("errorCode", errorCode);
        mav.addObject("errorMessage", message);
        return mav;
    }

    /**
     * URL không khớp handler nào (gõ sai đường dẫn, /favicon.ico...) là 404, không phải
     * lỗi hệ thống. Nếu để handler Exception bên dưới nuốt thì mọi cú gõ nhầm đều hiện
     * "500 lỗi hệ thống" và ghi cả stacktrace vào log, che mất lỗi thật.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNotFound(NoResourceFoundException ex) {
        log.debug("Không tìm thấy tài nguyên: {}", ex.getResourcePath());
        return errorView(HttpStatus.NOT_FOUND, 404, "Không tìm thấy trang bạn yêu cầu");
    }

    @ExceptionHandler(AppException.class)
    public ModelAndView handleAppException(AppException ex) {
        log.warn("AppException: {}", ex.getMessage());
        return errorView(ex.getErrorCode().getHttpStatus(),
                ex.getErrorCode().getCode(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return errorView(HttpStatus.BAD_REQUEST, 400, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneral(Exception ex) {
        // Che chi tiết lỗi với user nhưng phải log đầy đủ stacktrace để debug
        log.error("Lỗi hệ thống chưa được xử lý", ex);
        return errorView(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Đã xảy ra lỗi hệ thống");
    }
}
