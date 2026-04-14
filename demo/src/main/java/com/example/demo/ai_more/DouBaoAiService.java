package com.example.demo.ai_more;

import com.example.demo.dto.ai.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service("aiMoreDouBaoAiService")
@RequiredArgsConstructor
public class DouBaoAiService {

    private final com.example.demo.ai.DouBaoAiService delegate;

    public String sendToAi(String userText) {
        return delegate.sendToAi(userText);
    }

    public String sendToAiFast(String userText) {
        return delegate.sendToAiFast(userText);
    }

    public String sendToAiFast(List<ChatMessage> messages) {
        return delegate.sendToAiFast(messages);
    }

    public String sendToAiFastOrThrow(List<ChatMessage> messages) {
        return delegate.sendToAiFastOrThrow(messages);
    }

    public SseEmitter sendToAiStream(String userText) {
        return delegate.sendToAiStream(userText);
    }

    public SseEmitter sendToAiStreamFast(String userText) {
        return delegate.sendToAiStreamFast(userText);
    }

    public void sendToAiStreamFast(String userText, SseEmitter emitter) {
        delegate.sendToAiStreamFast(userText, emitter);
    }

    public SseEmitter sendToAiStreamFast(List<ChatMessage> messages) {
        return delegate.sendToAiStreamFast(messages);
    }

    public SseEmitter sendToAiStreamFast(List<ChatMessage> messages, String fallbackContent) {
        return delegate.sendToAiStreamFast(messages, fallbackContent);
    }

    public void sendToAiStreamFast(List<ChatMessage> messages, SseEmitter emitter) {
        delegate.sendToAiStreamFast(messages, emitter);
    }

    public void sendToAiStreamFast(List<ChatMessage> messages, SseEmitter emitter, String fallbackContent) {
        delegate.sendToAiStreamFast(messages, emitter, fallbackContent);
    }
}
