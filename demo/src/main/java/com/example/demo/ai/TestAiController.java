package com.example.demo.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 极简流式测试接口，无权限拦截，优先验证功能
 */
@RestController
@RequestMapping("/test-ai")
@RequiredArgsConstructor
public class TestAiController {

    private final DouBaoAiService douBaoAiService;

    /**
     * 先测这个！普通接口，验证豆包API、路径、Bean注入是否正常
     */
    @GetMapping("/normal")
    public String testNormal(@RequestParam String content) {
        return douBaoAiService.sendToAi(content);
    }

    /**
     * 流式测试接口，和你原有流式逻辑完全一致
     */
    @GetMapping("/stream")
    public SseEmitter testStream(@RequestParam String content) {
        return douBaoAiService.sendToAiStream(content);
    }
}