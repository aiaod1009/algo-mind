package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${file.upload-dir:./uploads/avatars}")
    private String uploadDir;

    @Value("${app.upload.chat-files:uploads/chat-files}")
    private String chatFilesDir;

    public WebMvcConfig() {
        log.info("WebMvcConfig 初始化");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 聊天文件访问路径
        Path chatFilesPath = Paths.get(chatFilesDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/chat-files/**")
                .addResourceLocations("file:" + chatFilesPath.toString() + "/");

        log.info("静态资源映射:");
        log.info("  /uploads/chat-files/** -> {}", chatFilesPath);
    }
}
