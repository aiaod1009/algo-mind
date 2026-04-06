package com.example.demo.controller;

import com.example.demo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

/**
 * 代码运行接口，统一使用全局Result类，修复参数/响应规范
 */
@RestController
public class CodeController {

    @PostMapping("/run-code")
    public Result<RunResult> runCode(@RequestBody CodeRequest req) {
        // 临时目录
        File tempDir = new File("temp_code");
        if (!tempDir.exists()) {
            boolean mkdirSuccess = tempDir.mkdirs();
            if (!mkdirSuccess) {
                return Result.fail(50001, "创建临时目录失败");
            }
        }

        String lang = req.getLanguage();
        String code = req.getCode();
        String output = "";
        String error = "";
        File tempFile = null;

        try {
            switch (lang) {
                case "cpp" -> {
                    tempFile = new File(tempDir, "temp.cpp");
                    Files.writeString(tempFile.toPath(), code);

                    // 编译
                    Process compile = Runtime.getRuntime().exec("g++ " + tempFile.getPath() + " -o " + tempDir.getPath() + "/temp");
                    error = readError(compile);
                    compile.waitFor();
                    if (!error.isBlank()) {
                        return Result.fail(40002, "编译失败：\n" + error);
                    }

                    // 运行
                    Process run = Runtime.getRuntime().exec(tempDir.getPath() + "/temp");
                    output = readOutput(run);
                    error = readError(run);
                    run.waitFor();
                }

                case "python" -> {
                    tempFile = new File(tempDir, UUID.randomUUID() + ".py");
                    Files.writeString(tempFile.toPath(), code);
                    Process run = Runtime.getRuntime().exec("python " + tempFile.getPath());
                    output = readOutput(run);
                    error = readError(run);
                }

                case "java" -> {
                    tempFile = new File(tempDir, "Temp.java");
                    Files.writeString(tempFile.toPath(), code);
                    Process compile = Runtime.getRuntime().exec("javac " + tempFile.getPath());
                    error = readError(compile);
                    compile.waitFor();
                    if (!error.isBlank()) {
                        return Result.fail(40002, "编译失败：\n" + error);
                    }

                    Process run = Runtime.getRuntime().exec("java -cp " + tempDir.getPath() + " Temp");
                    output = readOutput(run);
                    error = readError(run);
                }

                case "js" -> {
                    tempFile = new File(tempDir, UUID.randomUUID() + ".js");
                    Files.writeString(tempFile.toPath(), code);
                    Process run = Runtime.getRuntime().exec("node " + tempFile.getPath());
                    output = readOutput(run);
                    error = readError(run);
                }

                default -> {
                    return Result.fail(40001, "不支持的语言：" + lang);
                }
            }

            return Result.success(new RunResult(output, error));
        } catch (Exception e) {
            return Result.fail(50001, "运行失败：" + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean delete = tempFile.delete();
                if (!delete) {
                    System.err.println("临时文件清理失败：" + tempFile.getPath());
                }
            }
            // 清理编译产物（cpp/java）
            if ("cpp".equals(lang)) {
                File cppOut = new File(tempDir, "temp");
                if (cppOut.exists()) {
                    cppOut.delete();
                }
            } else if ("java".equals(lang)) {
                File classFile = new File(tempDir, "Temp.class");
                if (classFile.exists()) {
                    classFile.delete();
                }
            }
        }
    }

    /**
     * 读取进程标准输出
     */
    private String readOutput(Process p) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }

    /**
     * 读取进程错误输出
     */
    private String readError(Process p) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }

    // ------------------- 请求/返回结构（独立静态类，规范命名） -------------------
    public static class CodeRequest {
        private String language;
        private String code;

        // Getter & Setter
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
    }

    public static class RunResult {
        private String output;
        private String error;

        // 构造器 + Getter
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