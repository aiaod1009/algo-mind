package com.example.demo.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 独立的多模态测试接口（完全不影响原有业务代码）
 */
@RestController
@RequestMapping("/api/test/multimodal")
@RequiredArgsConstructor
@Tag(name = "多模态测试接口", description = "仅用于Swagger测试，不影响原有业务接口")
public class DouBaoMultimodalTestController {

    // 复用你已有的多模态服务
    private final DouBaoMultimodalService multimodalService;

    @Operation(
            summary = "非流式多模态对话测试",
            description = "传入文本+图片base64，一次性返回完整回答",
            responses = {
                    @ApiResponse(responseCode = "200", description = "调用成功，返回模型回答"),
                    @ApiResponse(responseCode = "400", description = "参数错误，请求格式非法")
            }
    )
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String testMultimodalChat(
            @RequestBody MultimodalTestRequest request
    ) {
        return multimodalService.chatWithImages(request.getContent(), request.getFiles());
    }

    @Operation(
            summary = "流式多模态对话测试(SSE)",
            description = "传入文本+图片base64，逐字返回流式结果",
            responses = {
                    @ApiResponse(responseCode = "200", description = "流式连接建立成功"),
                    @ApiResponse(responseCode = "400", description = "参数错误，请求格式非法")
            }
    )
    @PostMapping(value = "/chat/stream", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SseEmitter testMultimodalChatStream(
            @RequestBody MultimodalTestRequest request
    ) {
        return multimodalService.chatWithImagesStream(request.getContent(), request.getFiles());
    }

    /**
     * 【新增】专门用于Swagger测试的单文件上传接口，彻底解决Swagger UI校验报错
     */
    @Operation(
            summary = "非流式多模态对话（单文件上传版，推荐Swagger测试用）",
            description = "前端直接上传单张图片文件，无需手动转Base64，专门用于Swagger UI测试",
            responses = {
                    @ApiResponse(responseCode = "200", description = "调用成功，返回模型回答"),
                    @ApiResponse(responseCode = "400", description = "参数错误")
            }
    )
    @PostMapping(value = "/chat/file/swagger", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String testMultimodalChatWithSingleFileForSwagger(
            @Parameter(description = "用户输入的文本内容", example = "分析这张图片", required = true)
            @RequestParam("content") String content,

            @Parameter(
                    description = "上传的图片文件（支持jpg/png/webp）",
                    required = true,
                    schema = @Schema(type = "string", format = "binary")
            )
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            return "请上传有效的图片文件";
        }
        try {
            // 后端自动把文件转成Base64，适配原有Service
            List<DouBaoMultimodalService.ImageFile> imageFiles = new ArrayList<>();
            DouBaoMultimodalService.ImageFile img = new DouBaoMultimodalService.ImageFile();
            img.setName(file.getOriginalFilename());
            img.setBase64(Base64.getEncoder().encodeToString(file.getBytes()));
            imageFiles.add(img);

            return multimodalService.chatWithImages(content, imageFiles);
        } catch (Exception e) {
            return "图片处理失败：" + e.getMessage();
        }
    }

    /**
     * 【纯测试接口】仅验证：文件转Base64功能是否正常
     * 上传任意文件(图片/Word/PDF)，直接返回Base64结果，不调用AI
     */
    @Operation(summary = "纯测试：文件转Base64验证", description = "上传任意文件，直接返回Base64编码，验证转化功能是否成功")
    @PostMapping(value = "/test/base64", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String testFileToBase64(
            @Parameter(description = "上传任意文件（图片/Word/PDF/文本均可）", required = true, schema = @Schema(type = "string", format = "binary"))
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String fileName = file.getOriginalFilename();
            long fileSize = file.getSize();
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            return "✅ 文件转Base64转化成功！\n" +
                    "文件名：" + fileName + "\n" +
                    "文件大小：" + fileSize + " 字节\n" +
                    "Base64前100位：\n" + base64.substring(0, Math.min(100, base64.length()));
        } catch (Exception e) {
            return "❌ 转化失败：" + e.getMessage();
        }
    }

    /**
     * 独立的测试请求DTO，通过@Schema写文档说明
     */
    @Data
    @Schema(description = "多模态测试请求体")
    public static class MultimodalTestRequest {
        @Schema(description = "用户输入的文本内容", example = "描述一下这张图片里的内容")
        private String content;

        @Schema(description = "上传的图片列表", example = "[{\"fileName\":\"test.png\",\"base64\":\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+P+/HgAFeAJ5lM07FwAAAABJRU5ErkJggg==\"}]")
        private List<DouBaoMultimodalService.ImageFile> files;
    }
}