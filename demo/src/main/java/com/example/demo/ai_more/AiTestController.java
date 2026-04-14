package com.example.demo.ai_more;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("aiMoreAiTestController")
@RequiredArgsConstructor
public class AiTestController {

    private final DouBaoAiService douBaoAiService;

    @GetMapping("/ai-more/test/ai")
    public String testAi(@RequestParam String content) {
        return douBaoAiService.sendToAi(content);
    }
}
