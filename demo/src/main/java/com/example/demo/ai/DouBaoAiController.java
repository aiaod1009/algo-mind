package com.example.demo.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class DouBaoAiController {

    private final DouBaoMultimodalService multimodalService;

    @PostMapping("/multimodal")
    public String chatMultimodal(@RequestBody MultimodalRequest request) {
        return multimodalService.chatWithImages(request.getContent(), request.getFiles());
    }

    @PostMapping("/multimodal/stream")
    public SseEmitter chatMultimodalStream(@RequestBody MultimodalRequest request) {
        return multimodalService.chatWithImagesStream(request.getContent(), request.getFiles());
    }

    @GetMapping("/test-sse")
    public SseEmitter testSse() {
        SseEmitter emitter = new SseEmitter(60_000L);
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(1000);
                    emitter.send("第 " + i + " 条消息：SSE 流式推送功能正常\n");
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    public static class MultimodalRequest {
        private String content;
        private List<DouBaoMultimodalService.ImageFile> files;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<DouBaoMultimodalService.ImageFile> getFiles() {
            return files;
        }

        public void setFiles(List<DouBaoMultimodalService.ImageFile> files) {
            this.files = files;
        }
    }
}
