package com.example.demo.dto.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatDtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void chatRequestSerializesMaxTokensUsingSnakeCase() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .model("test-model")
                .messages(List.of(ChatMessage.user("hello")))
                .temperature(0.7)
                .maxTokens(256)
                .stream(false)
                .build();

        String json = objectMapper.writeValueAsString(request);

        assertThat(json).contains("\"max_tokens\":256");
        assertThat(json).doesNotContain("\"maxTokens\"");
    }

    @Test
    void chatResponseDeserializesSnakeCaseFieldsAndExtractsContent() throws Exception {
        String json = """
                {
                  "id": "chatcmpl-1",
                  "object": "chat.completion",
                  "created": 123456789,
                  "model": "test-model",
                  "choices": [
                    {
                      "index": 0,
                      "message": {
                        "role": "assistant",
                        "content": "hello"
                      },
                      "finish_reason": "stop"
                    }
                  ],
                  "usage": {
                    "prompt_tokens": 12,
                    "completion_tokens": 34,
                    "total_tokens": 46
                  },
                  "ignored_field": "ignored"
                }
                """;

        ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

        assertThat(response.getContent()).isEqualTo("hello");
        assertThat(response.getChoices()).hasSize(1);
        assertThat(response.getChoices().get(0).getFinishReason()).isEqualTo("stop");
        assertThat(response.getUsage().getPromptTokens()).isEqualTo(12);
        assertThat(response.getUsage().getCompletionTokens()).isEqualTo(34);
        assertThat(response.getUsage().getTotalTokens()).isEqualTo(46);
    }
}
