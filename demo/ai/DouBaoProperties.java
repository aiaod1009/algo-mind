package com.example.demo.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "doubao.api")
public class DouBaoProperties {
    private String key;
    private String url;
    private String model;
}