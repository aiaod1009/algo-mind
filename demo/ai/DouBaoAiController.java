package com.example.demo.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【仅新增】AI统一工具接口
 * 调用方式：只传 content 参数，直接返回AI结果
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class DouBaoAiController {

    // 直接注入你已有的、测试通过的Service
    private final DouBaoAiService douBaoAiService;

    /**
     * 极简AI工具接口
     * @param content 自定义传入的内容（提示词/问题）
     * @return AI返回结果
     */
    @PostMapping("/chat")
    public String chat(@RequestParam String content) {
        // 直接调用你原有测试通过的方法，无任何修改
        return douBaoAiService.sendToAi(content);
    }
}