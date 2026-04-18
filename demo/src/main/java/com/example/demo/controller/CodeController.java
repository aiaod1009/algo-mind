package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.service.Judge0Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CodeController {

    private final Judge0Service judge0Service;

    public CodeController(Judge0Service judge0Service) {
        this.judge0Service = judge0Service;
    }

    @PostMapping("/run-code")
    public Result<RunResult> runCode(@RequestBody CodeRequest req) {
        if (req == null || req.getLanguage() == null || req.getLanguage().isBlank()) {
            return Result.fail(40001, "Missing language");
        }
        if (req.getCode() == null || req.getCode().isBlank()) {
            return Result.fail(40001, "Missing code");
        }

        try {
            Map<String, Object> judgeResult = judge0Service.execute(
                    req.getLanguage(),
                    req.getCode(),
                    req.getStdinInput()
            );

            int statusId = (int) judgeResult.getOrDefault("status_id", 1);
            String stderr = (String) judgeResult.getOrDefault("stderr", "");
            String stdout = (String) judgeResult.getOrDefault("stdout", "");
            String compileOutput = (String) judgeResult.getOrDefault("compile_output", "");

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
