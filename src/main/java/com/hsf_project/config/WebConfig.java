package com.hsf_project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Không chặn /payment/vnpay-return: callback từ VNPay phải xử lý được
        // kể cả khi session hết hạn trong lúc user đang ở trang thanh toán
        // (bookingCode nằm trong vnp_TxnRef nên không cần session).
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/booking/**", "/customer/**");
        registry.addInterceptor(new AdminInterceptor()).addPathPatterns("/admin/**");
    }
}
