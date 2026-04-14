package com.example.demo.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "doubao.api")
public class DouBaoProperties {

    private static final String DEFAULT_ARK_BASE_URL = "https://ark.cn-beijing.volces.com/api/v3";

    private String key;
    private String url;
    private String baseUrl;
    private String multimodalUrl;
    private String multimodalKey;
    private String model;
    private Integer maxTokens = 1200;

    public String getMultimodalUrl() {
        if (multimodalUrl == null || multimodalUrl.isBlank()) {
            return DEFAULT_ARK_BASE_URL + "/responses";
        }
        return multimodalUrl;
    }

    public String getResponsesUrl() {
        return getMultimodalUrl();
    }

    public String getMultimodalApiKey() {
        if (multimodalKey != null && !multimodalKey.isBlank()) {
            return multimodalKey;
        }
        return key;
    }

    public String getSdkBaseUrl() {
        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl;
        }
        return deriveBaseUrl(url);
    }

    public String getMultimodalBaseUrl() {
        return deriveBaseUrl(getMultimodalUrl());
    }

    private String deriveBaseUrl(String fullUrl) {
        if (fullUrl == null || fullUrl.isBlank()) {
            return DEFAULT_ARK_BASE_URL;
        }
        if (fullUrl.endsWith("/chat/completions")) {
            return fullUrl.substring(0, fullUrl.length() - "/chat/completions".length());
        }
        if (fullUrl.endsWith("/responses")) {
            return fullUrl.substring(0, fullUrl.length() - "/responses".length());
        }
        return fullUrl;
    }
}
