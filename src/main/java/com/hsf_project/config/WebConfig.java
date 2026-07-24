package com.hsf_project.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Ép mọi request/response dùng UTF-8, chạy TRƯỚC mọi filter khác.
     * Thiếu nó, Tomcat giải mã body form theo charset mặc định nên mọi form POST
     * có tiếng Việt (đăng ký tài khoản, tên rạp, tên combo...) chết với
     * InvalidParameterException "Character decoding failed" -> trang lỗi 500.
     * Đặt tên bean khác 'characterEncodingFilter' để không đụng bean auto-config của Boot.
     */
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> utf8EncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);

        FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * Đăng ký AuthFilter (jakarta.servlet.Filter) cho toàn bộ request.
     * Filter tự quyết định path nào công khai / cần đăng nhập / cần role;
     * /payment/vnpay-return luôn công khai vì callback VNPay không có session.
     */
    @Bean
    public FilterRegistrationBean<AuthFilter> authFilter() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }


}
