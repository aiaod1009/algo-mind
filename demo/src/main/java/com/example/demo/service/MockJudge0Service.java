package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MockJudge0Service {
    
    private static final Logger log = LoggerFactory.getLogger(MockJudge0Service.class);

    public Map<String, Object> execute(String language, String sourceCode, String stdinInput) {
        log.info("Mock Judge0 - Language: {}, Code length: {}", language, sourceCode != null ? sourceCode.length() : 0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("status_id", 3); // Status 3 = Accepted
        result.put("time", "0.1");
        result.put("memory", 1024);
        
        String stdout = "";
        String stderr = "";
        
        String lang = language.toLowerCase();
        
        if (lang.equals("python") || lang.equals("py")) {
            stdout = executePython(sourceCode);
        } else if (lang.equals("javascript") || lang.equals("js")) {
            stdout = executeJavaScript(sourceCode);
        } else if (lang.equals("java")) {
            stdout = executeJava(sourceCode);
        } else if (lang.equals("c") || lang.equals("cpp")) {
            stdout = "// C/C++ execution simulated\nOutput: Hello from C/C++!";
        } else {
            stdout = "Language " + language + " execution simulated";
        }
        
        result.put("stdout", stdout);
        result.put("stderr", stderr);
        result.put("compile_output", null);
        
        return result;
    }
    
    private String executePython(String code) {
        StringBuilder output = new StringBuilder();
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("print(")) {
                String content = line.substring(7, line.length() - 1);
                content = content.replace("\"", "").replace("'", "");
                output.append(content).append("\n");
            }
        }
        
        return output.toString().trim();
    }
    
    private String executeJavaScript(String code) {
        StringBuilder output = new StringBuilder();
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("console.log(")) {
                String content = line.substring(12, line.length() - 2);
                content = content.replace("\"", "").replace("'", "");
                output.append(content).append("\n");
            }
        }
        
        return output.toString().trim();
    }
    
    private String executeJava(String code) {
        if (code.contains("System.out.println")) {
            return "Hello from Java!";
        }
        return "Java execution simulated";
    }
}
