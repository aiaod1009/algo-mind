package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.service.Judge0Service;
import com.example.demo.service.MockJudge0Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @PostMapping("/run-code")
    public Result<RunResult> runCode(@RequestBody CodeRequest req) {
        log.info("=== runCode request === language: {}, code length: {}, useMock: {}", req.getLanguage(), req.getCode() != null ? req.getCode().length() : 0, useMock);
        
        if (req == null || req.getLanguage() == null || req.getLanguage().isBlank()) {
            return Result.fail(40001, "Missing language");
        }
        if (req.getCode() == null || req.getCode().isBlank()) {
            return Result.fail(40001, "Missing code");
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
            
            String stderr = stderrObj != null ? stderrObj.toString() : "";
            String stdout = stdoutObj != null ? stdoutObj.toString() : "";
            String compileOutput = compileOutputObj != null ? compileOutputObj.toString() : "";

            log.info("=== Parsed output === stdout: '{}', stderr: '{}', compileOutput: '{}', statusId: {}", stdout, stderr, compileOutput, statusId);

            if (compileOutput != null && !compileOutput.isBlank()) {
                return Result.fail(40002, "Compilation failed:\n" + compileOutput);
            }

            if (statusId == 6) {
                return Result.fail(40002, "Compilation failed:\n" + (compileOutput != null ? compileOutput : "Unknown compilation error"));
            }

            return Result.success(new RunResult(stdout, stderr));
        } catch (IllegalArgumentException e) {
            return Result.fail(40001, "Unsupported language: " + req.getLanguage());
        } catch (Exception e) {
            return Result.fail(50001, "Execution failed: " + e.getMessage());
        }
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
