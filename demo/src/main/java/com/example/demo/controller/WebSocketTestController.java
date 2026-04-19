package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/ws/test")
@Tag(name = "WebSocket测试", description = "WebSocket功能测试接口")
public class WebSocketTestController {

    private final WebSocketController webSocketController;
    private final Map<String, WebSocketSession> testSessions = new ConcurrentHashMap<>();

    @Autowired
    public WebSocketTestController(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
    }

    @Operation(summary = "获取WebSocket连接地址", description = "返回WebSocket连接的URL地址")
    @GetMapping("/connection-url")
    public Map<String, String> getConnectionUrl() {
        return Map.of(
                "websocketUrl", "ws://localhost:8000/api/ws/chat",
                "sockJsUrl", "http://localhost:8000/api/ws/chat/sockjs"
        );
    }

    @Operation(summary = "测试WebSocket消息格式", description = "返回测试WebSocket消息的格式示例")
    @GetMapping("/message-format")
    public Map<String, Object> getMessageFormat() {
        return Map.of(
                "chatMessage", Map.of(
                        "type", "chat",
                        "content", "你好，这是一条测试消息",
                        "messageId", "test-123456"
                ),
                "pingMessage", Map.of(
                        "type", "ping"
                ),
                "clearMessage", Map.of(
                        "type", "clear"
                ),
                "cancelMessage", Map.of(
                        "type", "cancel",
                        "messageId", "test-123456"
                )
        );
    }

    @Operation(summary = "WebSocket测试说明", description = "提供WebSocket测试的详细说明")
    @GetMapping("/test-guide")
    public Map<String, String> getTestGuide() {
        return Map.of(
                "step1", "在Swagger UI中查看WebSocket连接地址",
                "step2", "使用WebSocket客户端工具（如Postman、WebSocket King Client等）连接到WebSocket地址",
                "step3", "发送测试消息，格式参考message-format接口返回的示例",
                "step4", "观察WebSocket服务器的响应",
                "step5", "测试不同类型的消息（chat、ping、clear、cancel）"
        );
    }
}
