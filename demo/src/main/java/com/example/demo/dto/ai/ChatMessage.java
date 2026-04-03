package com.example.demo.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI对话消息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    /**
     * 角色：system/user/assistant
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }
    
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }
    
    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }
}
