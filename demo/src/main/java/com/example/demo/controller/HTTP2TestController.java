package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/http2/test")
@Tag(name = "HTTP/2测试", description = "HTTP/2功能测试接口")
public class HTTP2TestController {

    @Operation(summary = "测试HTTP/2连接", description = "测试当前连接是否使用HTTP/2协议")
    @GetMapping("/connection")
    public Map<String, Object> testHTTP2Connection(HttpServletRequest request) {
        // 获取协议信息
        String protocol = request.getProtocol();
        String scheme = request.getScheme();
        int port = request.getServerPort();
        
        // 检查是否使用HTTP/2
        boolean isHTTP2 = protocol.equals("HTTP/2.0") || request.getHeader("Upgrade") != null;
        
        return Map.of(
                "protocol", protocol,
                "scheme", scheme,
                "port", port,
                "isHTTP2", isHTTP2,
                "message", isHTTP2 ? "HTTP/2连接成功" : "当前使用的是HTTP/1.x",
                "tips", "HTTP/2在使用HTTPS时效果最佳，当前测试使用的是HTTP"
        );
    }

    @Operation(summary = "HTTP/2性能测试", description = "测试HTTP/2的性能，返回当前时间戳")
    @GetMapping("/performance")
    public Map<String, Object> testHTTP2Performance() {
        long startTime = System.currentTimeMillis();
        
        // 模拟一些计算操作
        long sum = 0;
        for (int i = 0; i < 1000000; i++) {
            sum += i;
        }
        
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        
        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "elapsedTime", elapsedTime,
                "message", "性能测试完成",
                "sum", sum
        );
    }

    @Operation(summary = "HTTP/2信息", description = "返回HTTP/2相关信息和配置")
    @GetMapping("/info")
    public Map<String, Object> getHTTP2Info() {
        return Map.of(
                "enabled", true,
                "description", "HTTP/2已在application.yaml中启用",
                "benefits", "多路复用、头部压缩、服务器推送、优先级",
                "tips", "建议在生产环境中使用HTTPS来获得最佳HTTP/2性能"
        );
    }
}
