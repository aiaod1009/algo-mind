package com.example.demo.ai;

import com.volcengine.ark.runtime.model.responses.content.OutputContentItem;
import com.volcengine.ark.runtime.model.responses.content.OutputContentItemText;
import com.volcengine.ark.runtime.model.responses.item.BaseItem;
import com.volcengine.ark.runtime.model.responses.item.ItemOutputMessage;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;

final class ArkResponseHelper {

    private ArkResponseHelper() {
    }

    static String extractText(ResponseObject response) {
        if (response == null) {
            throw new IllegalStateException("AI response is empty");
        }
        if (response.getError() != null) {
            throw new IllegalStateException(response.getError().getMessage());
        }
        if (response.getOutput() == null || response.getOutput().isEmpty()) {
            throw new IllegalStateException("AI did not return any output");
        }

        StringBuilder builder = new StringBuilder();
        for (BaseItem item : response.getOutput()) {
            if (item instanceof ItemOutputMessage message && message.getContent() != null) {
                for (OutputContentItem contentItem : message.getContent()) {
                    if (contentItem instanceof OutputContentItemText textItem && textItem.getText() != null) {
                        builder.append(textItem.getText());
                    }
                }
            }
        }

        String text = builder.toString().trim();
        if (text.isEmpty()) {
            throw new IllegalStateException("AI returned output without text");
        }
        return text;
    }
}
