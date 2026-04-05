package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * IP 归属地配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ip.location")
public class IpLocationProperties {

    /**
     * 是否启用 IP 归属地解析。
     */
    private boolean enabled = true;

    /**
     * ip2region xdb 文件位置。
     */
    private String dbPath = "classpath:ipdb/ip2region.xdb";

    /**
     * 初始化失败时是否中断启动。
     */
    private boolean failFast = false;
}
