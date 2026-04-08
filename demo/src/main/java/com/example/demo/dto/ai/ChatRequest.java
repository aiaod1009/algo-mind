package com.example.demo.dto.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI聊天请求DTO（兼容OpenAI格式）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 对话消息列表
     */
    private List<ChatMessage> messages;
    
    /**
     * 温度参数（0-2）
     */
    private Double temperature;
    
    /**
     * 最大生成token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    /**
     * 是否流式输出
     */
    private Boolean stream;
}
