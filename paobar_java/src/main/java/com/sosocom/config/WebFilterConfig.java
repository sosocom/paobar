package com.sosocom.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFilterConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil) {
        return new JwtAuthFilter(jwtUtil);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>(jwtAuthFilter);
        bean.addUrlPatterns("/api/*");
        bean.setOrder(1);   // CORS=0 在前，JWT=1 在后
        return bean;
    }
}
