package com.example.demo.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 极简AI测试接口
 * 功能：只接收内容 → 调用AI工具 → 返回结果
 * 不修改任何原有代码！
 */
@RestController
@RequiredArgsConstructor
public class AiTestController {

    // 注入你测试通过的AI工具
    private final DouBaoAiService douBaoAiService;

    /**
     * 测试接口
     * @param content 只需要传这个内容
     * @return AI返回的结果
     */
    @GetMapping("/test/ai")
    public String testAi(@RequestParam String content) {
        // 调用你的工具类
        String result = douBaoAiService.sendToAi(content);
        // 输出结果（控制台打印 + 接口返回）
        System.out.println("AI返回结果：" + result);
        return result;
    }
}