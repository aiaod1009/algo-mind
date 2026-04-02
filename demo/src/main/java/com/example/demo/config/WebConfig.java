package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置：允许前端域名访问，匹配开发文档联调要求
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // 匹配所有接口前缀
                .allowedOriginPatterns("*")  // 测试环境允许所有来源（生产可指定前端域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 允许的请求方法
                .allowedHeaders("*")  // 允许所有请求头（包含Authorization）
                .allowCredentials(true)  // 允许携带Cookie
                .maxAge(3600);  // 预检请求缓存时间
    }
}