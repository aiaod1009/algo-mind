package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI服务配置类：读取豆包API配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "doubao.api")
public class AIConfig {
    
    /**
     * API密钥
     */
    private String key;
    
    /**
     * API接口地址
     */
    private String url;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 超时时间（毫秒）
     */
    private int timeout = 30000;
    
    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
    
    /**
     * 温度参数（控制生成随机性，0-2之间）
     */
    private double temperature = 0.7;
    
    /**
     * 最大生成token数
     */
    private int maxTokens = 4096;
}
