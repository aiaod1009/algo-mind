package com.example.demo.ai_more;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Base64;
import java.util.List;

@RestController("aiMoreDouBaoMultimodalTestController")
@RequestMapping("/api/test/ai-more/multimodal")
@RequiredArgsConstructor
@Tag(name = "AI More Multimodal Test")
public class DouBaoMultimodalTestController {

    private final DouBaoMultimodalService multimodalService;

    @Operation(summary = "Non-stream multimodal test")
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String testMultimodalChat(@RequestBody MultimodalTestRequest request) {
        return multimodalService.chatWithImages(request.getContent(), request.getFiles());
    }

    @Operation(summary = "Streaming multimodal test")
    @PostMapping(value = "/chat/stream", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SseEmitter testMultimodalChatStream(@RequestBody MultimodalTestRequest request) {
        return multimodalService.chatWithImagesStream(request.getContent(), request.getFiles());
    }

    @Operation(summary = "Upload one file and send it to multimodal chat")
    @PostMapping(value = "/chat/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String testMultimodalChatWithSingleFile(
            @Parameter(description = "User text")
            @RequestParam("content") String content,
            @Parameter(description = "Image file", schema = @Schema(type = "string", format = "binary"))
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        if (file.isEmpty()) {
            return "Please upload a valid image";
        }

        DouBaoMultimodalService.ImageFile image = new DouBaoMultimodalService.ImageFile();
        image.setName(file.getOriginalFilename());
        image.setType(file.getContentType());
        image.setBase64(Base64.getEncoder().encodeToString(file.getBytes()));
        image.setImage(true);
        image.setSize(file.getSize());

        return multimodalService.chatWithImages(content, List.of(image));
    }

    @Operation(summary = "Verify pure base64 extraction only")
    @PostMapping(value = "/extract-base64", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String extractPureBase64(@RequestBody String rawValue) {
        return DouBaoMultimodalService.extractPureBase64(rawValue);
    }

    @Data
    public static class MultimodalTestRequest {
        private String content;
        private List<DouBaoMultimodalService.ImageFile> files;
    }
}
