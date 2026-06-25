package com.recruitment.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter() {
        var bean = new FilterRegistrationBean<>(jwtAuthFilter);
        bean.addUrlPatterns("/api/*");
        bean.setOrder(1);
        return bean;
    }
}
