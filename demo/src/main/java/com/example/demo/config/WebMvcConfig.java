package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    public WebMvcConfig(@Value("${file.upload-dir:./uploads/avatars}") String uploadDir) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        log.info("上传文件已切换为受控下载模式");
        log.info("  上传目录: {}", uploadPath);
        log.info("  头像访问路径: /uploads/avatars/{filename}");
    }
}
