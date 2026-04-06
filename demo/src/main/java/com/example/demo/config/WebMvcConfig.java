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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path parentPath = uploadPath.getParent();
        Path staticRoot = parentPath != null ? parentPath : uploadPath;

        log.info("静态资源映射配置:");
        log.info("  上传目录: {}", uploadPath);
        log.info("  静态资源根目录: {}", staticRoot);
        log.info("  资源映射: /uploads/** -> {}", staticRoot.toUri());

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(staticRoot.toUri().toString());
    }
}
