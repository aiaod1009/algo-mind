package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局跨域配置 - Docker 部署
 *
 * 开发环境：application-dev.yml 中配置，或使用默认值
 * 生产环境：application-prod.yml 中配置，或通过环境变量 CORS_ALLOWED_ORIGINS 指定
 */
@Slf4j
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:}")
    private String allowedOriginsFromConfig;

    /**
     * 当前激活的 profile（dev / prod）
     * 没配置时默认当作 dev
     */
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)  // ★ 最高优先级，确保在 Security 之前执行
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 根据环境获取允许的源
        List<String> allowedOrigins = getAllowedOrigins();
        config.setAllowedOrigins(allowedOrigins);

        // 允许的方法
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // ★ 允许所有请求头（防止遗漏导致上传失败）
        config.setAllowedHeaders(List.of("*"));

        // 允许前端读取的响应头
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));

        // 允许携带凭证
        config.setAllowCredentials(true);

        // 预检缓存
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        log.info("====== CORS 配置已加载 [{}] ======", activeProfile);
        log.info("允许的源: {}", allowedOrigins);

        return new CorsFilter(source);
    }

    private List<String> getAllowedOrigins() {

        // 优先级1：环境变量（Docker / 云服务器部署）
        String envOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (envOrigins != null && !envOrigins.trim().isEmpty()) {
            log.info("CORS 来源: 环境变量");
            return parseOrigins(envOrigins);
        }

        // 优先级2：配置文件（application-xxx.yml）
        if (allowedOriginsFromConfig != null && !allowedOriginsFromConfig.trim().isEmpty()) {
            log.info("CORS 来源: 配置文件");
            return parseOrigins(allowedOriginsFromConfig);
        }

        // 优先级3：默认值
        // ★ 生产环境不给默认值，强制必须配置
        if ("prod".equals(activeProfile)) {
            log.error("⚠️ 生产环境未配置 CORS 允许源！请设置环境变量 CORS_ALLOWED_ORIGINS 或在 application-prod.yml 中配置 cors.allowed-origins");
            // 生产环境不配置就只允许同源，不给 localhost
            return List.of();
        }

        // 开发环境默认值
        log.info("CORS 来源: 开发环境默认值");
        return Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5175",
                "http://localhost:5176",
                "http://localhost:5177",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174",
                "http://127.0.0.1:5175",
                "http://127.0.0.1:5176",
                "http://127.0.0.1:5177"
        );
    }

    /**
     * 解析逗号分隔的 origins 字符串
     * 去除空格，过滤空值
     */
    private List<String> parseOrigins(String origins) {
        return Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
