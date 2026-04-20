package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.service.Judge0Service;
import com.example.demo.service.MockJudge0Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CodeController {

    private static final Logger log = LoggerFactory.getLogger(CodeController.class);
    private final Judge0Service judge0Service;
    private final MockJudge0Service mockJudge0Service;

    @Value("${judge0.use-mock:false}")
    private boolean useMock;

    public CodeController(Judge0Service judge0Service, MockJudge0Service mockJudge0Service) {
        this.judge0Service = judge0Service;
        this.mockJudge0Service = mockJudge0Service;
    }

    private static final int MAX_OUTPUT_LENGTH = 65536;
    private static final int TRUNCATE_THRESHOLD = 60000;
    private static final int MAX_CODE_LENGTH = 65536;

    @PostMapping("/run-code")
    public Result<RunResult> runCode(@RequestBody CodeRequest req) {
        log.info("=== runCode request === language: {}, code length: {}, useMock: {}", req.getLanguage(), req.getCode() != null ? req.getCode().length() : 0, useMock);
        
        if (req == null || req.getLanguage() == null || req.getLanguage().isBlank()) {
            return Result.fail(40001, "Missing language");
        }
        if (req.getCode() == null || req.getCode().isBlank()) {
            return Result.fail(40001, "Missing code");
        }
        if (req.getCode().length() > MAX_CODE_LENGTH) {
            return Result.fail(40001, "代码过长，最大支持 " + MAX_CODE_LENGTH + " 个字符");
        }

        try {
            Map<String, Object> judgeResult;
            
            if (useMock) {
                log.info("Using Mock Judge0 Service");
                judgeResult = mockJudge0Service.execute(
                        req.getLanguage(),
                        req.getCode(),
                        req.getStdinInput()
                );
            } else {
                log.info("Using Real Judge0 Service");
                judgeResult = judge0Service.execute(
                        req.getLanguage(),
                        req.getCode(),
                        req.getStdinInput()
                );
            }

            log.info("=== Judge0 result === {}", judgeResult);

            int statusId = (int) judgeResult.getOrDefault("status_id", 1);
            Object stderrObj = judgeResult.get("stderr");
            Object stdoutObj = judgeResult.get("stdout");
            Object compileOutputObj = judgeResult.get("compile_output");
            
            String stderr = truncateOutput(stderrObj != null ? stderrObj.toString() : "");
            String stdout = truncateOutput(stdoutObj != null ? stdoutObj.toString() : "");
            String compileOutput = compileOutputObj != null ? compileOutputObj.toString() : "";

            log.info("=== Parsed output === stdout length: {}, stderr length: {}, compileOutput length: {}, statusId: {}", stdout.length(), stderr.length(), compileOutput.length(), statusId);

            if (statusId == 5) {
                return Result.fail(50002, "运行超时（Time Limit Exceeded），请检查代码是否存在死循环或低效操作");
            }

            if (statusId == 6) {
                return Result.fail(40002, "编译失败:\n" + remapCompileErrors(compileOutput, req.getLanguage()));
            }

            if (compileOutput != null && !compileOutput.isBlank()) {
                return Result.fail(40002, "编译失败:\n" + remapCompileErrors(compileOutput, req.getLanguage()));
            }

            if (statusId == 7) {
                return Result.success(new RunResult("", "运行时错误（Runtime Error），请检查数组越界、空指针等问题"));
            }

            return Result.success(new RunResult(stdout, stderr));
        } catch (IllegalArgumentException e) {
            return Result.fail(40001, "Unsupported language: " + req.getLanguage());
        } catch (Exception e) {
            return Result.fail(50001, "Execution failed: " + e.getMessage());
        }
    }

    @GetMapping("/judge0/cluster-status")
    public Result<Map<String, Object>> clusterStatus() {
        return Result.success(judge0Service.getClusterStatus());
    }

    private String truncateOutput(String output) {
        if (output == null || output.isEmpty()) {
            return "";
        }
        if (output.length() > MAX_OUTPUT_LENGTH) {
            return output.substring(0, TRUNCATE_THRESHOLD) + "\n\n... [输出超过限制，已截断，共 " + output.length() + " 字符]";
        }
        return output;
    }

    private String remapCompileErrors(String compileOutput, String language) {
        if (compileOutput == null || compileOutput.isEmpty()) {
            return "Unknown compilation error";
        }
        String result = compileOutput;
        if ("c".equalsIgnoreCase(language) || "cpp".equalsIgnoreCase(language)) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("line\\s*(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(result);
            java.lang.StringBuffer sb = new java.lang.StringBuffer();
            while (matcher.find()) {
                int originalLine = Integer.parseInt(matcher.group(1));
                int shiftedLine = Math.max(1, originalLine - 3);
                matcher.appendReplacement(sb, "line " + shiftedLine);
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }

    public static class CodeRequest {
        private String language;
        private String code;
        private String stdinInput;

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getStdinInput() {
            return stdinInput;
        }

        public void setStdinInput(String stdinInput) {
            this.stdinInput = stdinInput;
        }
    }

    public static class RunResult {
        private final String output;
        private final String error;

        public RunResult(String output, String error) {
            this.output = output;
            this.error = error;
        }

        public String getOutput() {
            return output;
        }

        public String getError() {
            return error;
        }
    }
}
