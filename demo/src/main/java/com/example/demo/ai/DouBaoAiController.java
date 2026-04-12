package com.example.demo.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class DouBaoAiController {

    private final DouBaoAiService douBaoAiService;

    // 原有普通接口（不动）
    @PostMapping("/chat")
    public String chat(@RequestParam String content) {
        return douBaoAiService.sendToAi(content);
    }

    // ====================== 最终版：AI流式接口 ======================
    @PostMapping("/chat/stream")
    public SseEmitter chatStream(@RequestParam String content) {
        return douBaoAiService.sendToAiStream(content);
    }
    // ====================== 【极致快速模式】普通输出接口 ======================
    @PostMapping("/chat/fast")
    public String chatFast(@RequestParam String content) {
        return douBaoAiService.sendToAiFast(content);
    }

    // ====================== 【极致快速模式】流式输出接口 ======================
    @PostMapping("/chat/stream-fast")
    public SseEmitter chatStreamFast(@RequestParam String content) {
        return douBaoAiService.sendToAiStreamFast(content);
    }
    // 原有测试接口（不动）
    @GetMapping("/test-sse")
    public SseEmitter testSse() {
        SseEmitter emitter = new SseEmitter(60000L);
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(1000);
                    emitter.send("第" + i + "条消息：SSE流式推送功能正常！\n");
                    System.out.println("已推送第" + i + "条消息");
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}