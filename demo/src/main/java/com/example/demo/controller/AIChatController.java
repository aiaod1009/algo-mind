package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.dto.ai.ChatMessage;
import com.example.demo.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI对话接口
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIChatController {

    private final AIService aiService;

    private static final String SYSTEM_PROMPT = """
        你是AlgoMind的学习助手，专注于帮助用户学习算法和数据结构。
        
        你的职责：
        1. 解答算法和数据结构相关的问题
        2. 分析用户的错题，给出改进建议
        3. 推荐适合用户当前水平的练习题目
        4. 提供学习建议和鼓励
        
        回答要求：
        - 使用中文回答
        - 回答简洁明了，重点突出
        - 适当使用emoji让回答更友好
        - 如果用户问的是编程问题，可以给出代码示例
        - 鼓励用户坚持学习
        """;

    /**
     * AI对话接口
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        
        if (message == null || message.isBlank()) {
            return Result.fail(40001, "消息不能为空");
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(SYSTEM_PROMPT));
        
        if (history != null && !history.isEmpty()) {
            int startIndex = Math.max(0, history.size() - 10);
            for (int i = startIndex; i < history.size(); i++) {
                Map<String, String> msg = history.get(i);
                String role = msg.get("role");
                String content = msg.get("content");
                if ("user".equals(role)) {
                    messages.add(ChatMessage.user(content));
                } else if ("assistant".equals(role)) {
                    messages.add(ChatMessage.assistant(content));
                }
            }
        }
        
        messages.add(ChatMessage.user(message));

        String response = aiService.chat(messages);

        Map<String, Object> result = new HashMap<>();
        result.put("response", response);
        result.put("timestamp", System.currentTimeMillis());
        
        return Result.success(result);
    }
}
