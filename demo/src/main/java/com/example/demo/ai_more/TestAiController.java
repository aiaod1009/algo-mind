package com.example.demo.ai_more;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController("aiMoreTestAiController")
@RequestMapping("/ai-more/test-ai")
@RequiredArgsConstructor
public class TestAiController {

    private final DouBaoAiService douBaoAiService;

    @GetMapping("/normal")
    public String testNormal(@RequestParam String content) {
        return douBaoAiService.sendToAi(content);
    }

    @GetMapping("/stream")
    public SseEmitter testStream(@RequestParam String content) {
        return douBaoAiService.sendToAiStream(content);
    }
}
