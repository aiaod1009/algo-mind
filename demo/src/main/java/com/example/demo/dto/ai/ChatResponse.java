package com.example.demo.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI聊天响应DTO（兼容OpenAI格式）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {
    
    /**
     * 响应ID
     */
    private String id;
    
    /**
     * 对象类型
     */
    private String object;
    
    /**
     * 创建时间戳
     */
    private Long created;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 选择列表
     */
    private List<Choice> choices;
    
    /**
     * Token使用情况
     */
    private Usage usage;
    
    /**
     * 获取AI回复内容
     */
    public String getContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getContent();
        }
        return null;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Integer index;
        private ChatMessage message;
        @JsonProperty("finish_reason")
        private String finishReason;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
