package com.example.demo.controller;

import com.example.demo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class CodeController {

    private static final Charset PROCESS_OUTPUT_FALLBACK_CHARSET = Charset.forName("GBK");
    private static final Pattern JAVA_PUBLIC_CLASS_PATTERN =
            Pattern.compile("\\bpublic\\s+class\\s+([A-Za-z_$][\\w$]*)");
    private static final Pattern JAVA_CLASS_PATTERN =
            Pattern.compile("\\bclass\\s+([A-Za-z_$][\\w$]*)");

    @PostMapping("/run-code")
    public Result<RunResult> runCode(@RequestBody CodeRequest req) {
        if (req == null || req.getLanguage() == null || req.getLanguage().isBlank()) {
            return Result.fail(40001, "Missing language");
        }
        if (req.getCode() == null || req.getCode().isBlank()) {
            return Result.fail(40001, "Missing code");
        }

        File runDir = null;
        try {
            runDir = createRunDirectory();
            String language = req.getLanguage().trim().toLowerCase(Locale.ROOT);
            RunResult result = switch (language) {
                case "cpp" -> runCpp(req.getCode(), req.getStdinInput(), runDir);
                case "python" -> runPython(req.getCode(), req.getStdinInput(), runDir);
                case "java" -> runJava(req.getCode(), req.getStdinInput(), runDir);
                case "js" -> runJavaScript(req.getCode(), req.getStdinInput(), runDir);
                default -> null;
            };

            if (result == null) {
                return Result.fail(40001, "Unsupported language: " + req.getLanguage());
            }

            return Result.success(result);
        } catch (CompilationException e) {
            return Result.fail(40002, "Compilation failed:\n" + e.getMessage());
        } catch (Exception e) {
            return Result.fail(50001, "Execution failed: " + e.getMessage());
        } finally {
            deleteRecursively(runDir);
        }
    }

    private RunResult runCpp(String code, String stdinInput, File runDir)
            throws IOException, InterruptedException, CompilationException {
        File sourceFile = new File(runDir, "main.cpp");
        String executableName = isWindows() ? "main.exe" : "main";
        File executableFile = new File(runDir, executableName);

        Files.writeString(sourceFile.toPath(), code, StandardCharsets.UTF_8);

        ProcessResult compileResult = execute(
                List.of("g++", sourceFile.getName(), "-o", executableFile.getName()),
                runDir,
                null
        );
        ensureCompiled(compileResult);

        ProcessResult runResult = execute(List.of(executableFile.getAbsolutePath()), runDir, stdinInput);
        return new RunResult(runResult.stdout(), runResult.stderr());
    }

    private RunResult runPython(String code, String stdinInput, File runDir)
            throws IOException, InterruptedException {
        File sourceFile = new File(runDir, "main.py");
        Files.writeString(sourceFile.toPath(), code, StandardCharsets.UTF_8);

        ProcessResult runResult = execute(List.of("python", sourceFile.getName()), runDir, stdinInput);
        return new RunResult(runResult.stdout(), runResult.stderr());
    }

    private RunResult runJava(String code, String stdinInput, File runDir)
            throws IOException, InterruptedException, CompilationException {
        String mainClassName = resolveJavaMainClassName(code);
        File sourceFile = new File(runDir, mainClassName + ".java");

        Files.writeString(sourceFile.toPath(), code, StandardCharsets.UTF_8);

        ProcessResult compileResult = execute(
                List.of("javac", "-encoding", StandardCharsets.UTF_8.name(), sourceFile.getName()),
                runDir,
                null
        );
        ensureCompiled(compileResult);

        ProcessResult runResult = execute(List.of("java", "-cp", runDir.getAbsolutePath(), mainClassName), runDir, stdinInput);
        return new RunResult(runResult.stdout(), runResult.stderr());
    }

    private RunResult runJavaScript(String code, String stdinInput, File runDir)
            throws IOException, InterruptedException {
        File sourceFile = new File(runDir, "main.js");
        Files.writeString(sourceFile.toPath(), code, StandardCharsets.UTF_8);

        ProcessResult runResult = execute(List.of("node", sourceFile.getName()), runDir, stdinInput);
        return new RunResult(runResult.stdout(), runResult.stderr());
    }

    private File createRunDirectory() throws IOException {
        File tempRoot = new File("temp_code");
        if (!tempRoot.exists() && !tempRoot.mkdirs()) {
            throw new IOException("Failed to create temp_code directory");
        }

        File runDir = new File(tempRoot, UUID.randomUUID().toString());
        if (!runDir.mkdirs()) {
            throw new IOException("Failed to create temporary run directory");
        }
        return runDir;
    }

    private ProcessResult execute(List<String> command, File workingDir, String stdinInput)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDir);

        Process process = processBuilder.start();
        CompletableFuture<String> stdoutFuture = readStreamAsync(process.getInputStream());
        CompletableFuture<String> stderrFuture = readStreamAsync(process.getErrorStream());

        writeStdin(process, stdinInput);

        int exitCode = process.waitFor();
        String stdout = stdoutFuture.join().trim();
        String stderr = stderrFuture.join().trim();
        return new ProcessResult(exitCode, stdout, stderr);
    }

    private CompletableFuture<String> readStreamAsync(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return readStream(inputStream);
            } catch (IOException e) {
                throw new CompletionException(new UncheckedIOException(e));
            }
        });
    }

    private String readStream(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        String utf8Text = new String(bytes, StandardCharsets.UTF_8);
        String fallbackText = new String(bytes, PROCESS_OUTPUT_FALLBACK_CHARSET);
        return countReplacementCharacters(utf8Text) <= countReplacementCharacters(fallbackText)
                ? utf8Text
                : fallbackText;
    }

    private void writeStdin(Process process, String stdinInput) throws IOException {
        try (OutputStream outputStream = process.getOutputStream()) {
            if (stdinInput != null && !stdinInput.isEmpty()) {
                outputStream.write(stdinInput.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        }
    }

    private void ensureCompiled(ProcessResult compileResult) throws CompilationException {
        if (compileResult.exitCode() != 0 || !compileResult.stderr().isBlank()) {
            String message = compileResult.stderr().isBlank() ? compileResult.stdout() : compileResult.stderr();
            throw new CompilationException(message.isBlank() ? "Unknown compiler error" : message);
        }
    }

    private String resolveJavaMainClassName(String code) {
        Matcher publicClassMatcher = JAVA_PUBLIC_CLASS_PATTERN.matcher(code);
        if (publicClassMatcher.find()) {
            return publicClassMatcher.group(1);
        }

        Matcher classMatcher = JAVA_CLASS_PATTERN.matcher(code);
        if (classMatcher.find()) {
            return classMatcher.group(1);
        }

        return "Main";
    }

    private int countReplacementCharacters(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i += 1) {
            if (text.charAt(i) == '\uFFFD') {
                count += 1;
            }
        }
        return count;
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }

        if (!file.delete()) {
            file.deleteOnExit();
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

    private record ProcessResult(int exitCode, String stdout, String stderr) {
    }

    private static class CompilationException extends Exception {
        private CompilationException(String message) {
            super(message);
        }
    }
}
